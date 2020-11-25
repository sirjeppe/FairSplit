const url = require('url');
const fs = require('fs');
const path = require('path');
const debugPrintouts = true;

class API {
  constructor(db) {
    this.db = db;
    this.backendModules = [];
    this.initBackendModules();
  }

  _getClassNameFromModule(moduleName) {
    let noFileSuffix = moduleName.replace('.class.js', '');
    let parts = noFileSuffix.split('-');
    for (let i = 0; i < parts.length; i++) {
      parts[i] = parts[i][0].toUpperCase() + parts[i].substring(1);
    }
    return parts.join('');
  }

  _findModuleFromRoute(route) {
    let handler = url.parse(route.substring('/api/'.length)).pathname.split('/')[0];
    for (let module of this.backendModules) {
      if (module.apiEndPoint && module.apiEndPoint.startsWith(`/${handler}`)) {
        return module;
      }
    }
    return null;
  }

  initBackendModules() {
    let files = fs.readdirSync(__dirname);
    for (let file of files) {
      if (file.endsWith('.class.js') && file !== 'backend-module.class.js') {
        let absolutePath = path.join(__dirname, file);
        delete require.cache[require.resolve(absolutePath)];
        let module = require(absolutePath);
        let className = this._getClassNameFromModule(file);
        try {
          console.log(`Loading module ${className}`);
          this.backendModules.push(eval(`new module.${className}(this)`));
        } catch {
          console.log(`ERROR! Failed to load module: ${file}`);
        }
      }
    }
  }

  route(request, response) {
    let module = this._findModuleFromRoute(request.url);
    if (module) {
      module.handle(request, response);
    } else {
      console.log(`ERROR! No module found for URL: ${request.url}`);
    }
  }
}

module.exports = API;

// OLD STUFF BELOW

// const requests = [
//   './requestObjects/BaseRequest.js'
// ];
// const responses = [
//   './responseObjects/BaseResponse.js',
//   './responseObjects/Error.js',
//   './responseObjects/User.js',
//   './responseObjects/Group.js',
//   './responseObjects/Expense.js'
// ];
// for (let i = 0; i < responses.length; i++) {
//   delete require.cache[require.resolve(responses[i])];
// }

// const BaseResponse = require(responses[0]);
// const Error = require(responses[1]);
// const User = require(responses[2]);
// const Group = require(responses[3]);
// const Expense = require(responses[4]);

// let api = {
//   respond: function(request, response, data) {
//     let code = 200;
//     let temp = data;
//     if (typeof(data) === 'string') {
//       data = new Error.ErrorResponse();
//       data.requestUri = request.url;
//       data.message = temp;
//       data.errorCode = 100; // Uknown error
//       code = 400;
//     } else if (typeof(data) === 'number') {
//       data = new Error.ErrorResponse();
//       data.requestUri = request.url;
//       data.errorCode = temp;
//       data.message = Error.ErrorMessages[temp];
//       code = 400;
//     } else if (typeof(data) === 'boolean') {
//       data = new BaseResponse();
//       data.requestUri = request.url;
//       data.errorCode = (temp) ? 0 : Error.ErrorCodes.COMMAND_FAILED;
//       data.message = (temp) ? 'Command successful' : Error.ErrorMessages[data.errorCode];
//       code = (temp) ? code : 400;
//     } else if (data instanceof BaseResponse) {
//       // Strip out db property from data objects if there are any before sending
//       // back the response
//       for (let i = 0; i < data.data.length; i++) {
//         if ('db' in data.data[i]) {
//           delete data.data[i].db;
//         }
//       }
//       if (data instanceof Error.ErrorResponse) {
//         code = 400;
//       }
//     }
//     response.writeHead(code, {'Content-Type': 'application/json'});
//     response.write(JSON.stringify(data));
//     response.end();

//     // DEBUG
//     if (debugPrintouts) {
//       console.log();
//       console.log('======== START RESPONSE ========');
//       console.log(JSON.stringify(data));
//       console.log('======== END RESPONSE BODY ========');
//     }
//     // END DEBUG
//   },

//   handle: function(db, request, response) {
//     let handler = url.parse(request.url.substring('/api/'.length)).pathname.split('/')[0];

//     let body = {};
//     let bodyArray = [];
//     request.on('data', (chunk) => {
//       bodyArray.push(chunk);
//     }).on('end', () => {
//       let bodyString = Buffer.concat(bodyArray).toString();
//       try {
//         body = JSON.parse(bodyString);
//       } catch (err) {
//         body = {};
//       }

//       // DEBUG
//       if (debugPrintouts) {
//         console.log();
//         console.log('======== START REQUEST ========');
//         console.log(request.method, request.url);
//         console.log('======== HEADERS ========');
//         console.log(request.headers);
//         if (request.method === 'POST' || request.method === 'PUT') {
//           console.log('======== BODY ========');
//           let printBody = Object.assign({}, body);
//           if ('password' in printBody) {
//             delete printBody.password;
//           }
//           if ('passwordAgain' in printBody) {
//             delete printBody.passwordAgain;
//           }
//           console.log(JSON.stringify(printBody));
//         }
//         console.log('======== END REQUEST ========');
//       }
//       // END DEBUG

//       // Validate API key
//       api.validateApiKey(db, request, (validationResponse) => {
//         if (typeof(validationResponse) === 'string') {
//           if (handler === 'login') {
//             api.login(db, request, response, body, (res) => {
//               if (res instanceof User.User) {
//                 let r = new User.UserResponse();
//                 r.requestUri = request.url;
//                 r.data.push(res);
//                 api.respond(request, response, r);
//               } else {
//                 api.respond(request, response, res);
//               }
//             });
//           } else if (handler === 'register') {
//             api.register(db, request, response, body, (res) => {
//               if (res instanceof User.User) {
//                 let r = new User.UserResponse();
//                 r.requestUri = request.url;
//                 r.data.push(res);
//                 api.respond(request, response, r);
//               } else {
//                 api.respond(request, response, res);
//               }
//             });
//           } else {
//             api.respond(request, response, validationResponse);
//           }
//         } else {
//           if (handler === 'user') {
//             api.user(db, request, response, body, (res) => {
//               if (res instanceof User.User) {
//                 let r = new User.UserResponse();
//                 r.requestUri = request.url;
//                 r.data.push(res);
//                 api.respond(request, response, r);
//               } else {
//                 api.respond(request, response, res);
//               }
//             });
//           } else if (handler === 'group') {
//             api.group(db, request, response, body, (res) => {
//               if (res instanceof Group.Group) {
//                 let r = new Group.GroupResponse();
//                 r.requestUri = request.url;
//                 r.data.push(res);
//                 api.respond(request, response, r);
//               } else {
//                 api.respond(request, response, res);
//               }
//             });
//           } else if (handler === 'expense') {
//             api.expense(db, request, response, body, (res) => {
//               if (res instanceof Array) {
//                 let r = new Expense.ExpenseResponse();
//                 r.requestUri = request.url;
//                 r.data = res;
//                 api.respond(request, response, r);
//               } else if (res instanceof Expense.Expense) {
//                 let r = new Expense.ExpenseResponse();
//                 r.requestUri = request.url;
//                 r.data.push(res);
//                 api.respond(request, response, r);
//               } else {
//                 api.respond(request, response, res);
//               }
//             });
//           } else {
//             api.respond(request, response, 'No handler found for request: ' + request.url);
//           }
//         }
//       });
//     });
//   },

//   validateApiKey: function(db, request, callback) {
//     if (!('fairsplit-apikey' in request.headers)) {
//       callback('fairsplit-apikey header missing');
//       return;
//     }
//     let now = Math.floor(Date.now() / 1000);
//     db.get(
//       'SELECT * FROM users WHERE apiKey = ? AND keyValidTo >= ?',
//       [request.headers['fairsplit-apikey'], now],
//       function(err, row) {
//         if (err || row === undefined) {
//           callback('Failed to validate the API key!');
//         } else {
//           // For each successful request, prolong validity by 2 weeks
//           let twoWeeks = 60 * 60 * 24 * 14;
//           let newValidTime = parseInt(now + twoWeeks);
//           db.run(
//             'UPDATE users SET keyValidTo = ? WHERE userID = ?',
//             [newValidTime, row.userID],
//             function(err) {
//               if (err) {
//                 console.error('Failed to set new valid time', err, 'for userID', row.userID);
//               }
//             }
//           );
//           callback(true);
//         }
//       }
//     );
//   },

//   login: function(db, request, response, body, callback) {
//     if (
//       typeof(body.userName) === 'undefined'
//       || typeof(body.password) === 'undefined'
//     ) {
//       callback(Error.ErrorCodes.NO_BODY);
//     } else {
//       let u = new User.User();
//       u.useDB(db);
//       u.login(body.userName, body.password, (res) => {
//         callback(res);
//       });
//     }
//   },

//   register: function(db, request, response, body, callback) {
//     if (
//       typeof(body.userName) === 'undefined'
//       || typeof(body.password) === 'undefined'
//       || typeof(body.passwordAgain) === 'undefined'
//     ) {
//       callback(Error.ErrorCodes.MALFORMED_REQUEST);
//     } else {
//       if (body.password !== body.passwordAgain) {
//         callback(Error.ErrorCodes.PASSWORDS_MISMATCH);
//         return;
//       }
//       let u = new User.User();
//       u.useDB(db);
//       u.register(body.userName, body.password, (res) => {
//         callback(res);
//       });
//     }
//   },

//   user: function(db, request, response, body, callback) {
//     let pathArray = request.url.split('/');
//     let userID = parseInt(pathArray[pathArray.length - 1]);
//     if (request.method === 'GET') {
//       if (userID > 0) {
//         let u = new User.User();
//         u.useDB(db);
//         u.getByID(userID, request.headers['fairsplit-apikey'], (res) => {
//           callback(res);
//         });
//       } else {
//         callback(Error.ErrorCodes.MALFORMED_REQUEST);
//       }
//     } else if (request.method === 'PUT') {
//       if (
//         typeof(body.income) === 'undefined'
//         || typeof(userID) !== 'number'
//         || userID <= 0
//       ) {
//         callback(Error.ErrorCodes.MALFORMED_REQUEST);
//       } else {
//         let u = new User.User();
//         u.useDB(db);
//         // userID and apiKey are needed for validation
//         u.userID = userID;
//         u.apiKey = request.headers['fairsplit-apikey'];
//         // income can be updated
//         u.income = body.income;
//         u.save((res) => {
//           callback(res);
//         });
//       }
//     }
//   },

//   group: function(db, request, response, body, callback) {
//     let pathArray = request.url.split('/');
//     let groupID = parseInt(pathArray[pathArray.length - 1]);
//     if (request.method === 'GET') {
//       if (
//         typeof(groupID) !== 'number'
//         || groupID <= 0
//       ) {
//         callback(Error.ErrorCodes.MALFORMED_REQUEST);
//       } else {
//         let g = new Group.Group();
//         g.useDB(db);
//         g.getByID(groupID, (res) => {
//           callback(res);
//         });
//       }
//     } else if (request.method === 'POST') {
//       if (
//         typeof(body.groupName) === 'undefined'
//         || typeof(groupID) !== 'number'
//         || groupID != -1
//       ) {
//         callback(Error.ErrorCodes.MALFORMED_REQUEST);
//       } else {
//         let g = new Group.Group();
//         g.useDB(db);
//         g.register(body.userID, body.groupName, (res) => {
//           callback(res);
//         });
//       }
//     } else if (request.method === 'PUT') {
//       if (
//         typeof(body.groupName) === 'undefined'
//         || typeof(groupID) !== 'number'
//         || groupID <= 0
//       ) {
//         callback(Error.ErrorCodes.MALFORMED_REQUEST);
//       } else {
//         let g = new Group.Group();
//         g.useDB(db);
//         // groupID and owner are needed for validation
//         g.groupID = groupID;
//         g.owner = body.owner;
//         // groupName and members can be updated
//         g.groupName = body.groupName;
//         g.members = body.members;
//         // Finally save, and verify against used apiKey
//         g.save(request.headers['fairsplit-apikey'], (res) => {
//           callback(res);
//         });
//       }
//     } else if (request.method === 'DELETE') {
//       if (
//         typeof(body.groupName) === 'undefined'
//         || typeof(groupID) !== 'number'
//         || groupID <= 0
//       ) {
//         callback(Error.ErrorCodes.MALFORMED_REQUEST);
//       } else {
//         Group.Group.deleteByID(db, groupID, (res) => {
//           callback(res);
//         });
//       }
//     }
//   },

//   _verifyExpenseBody: function(body) {
//     if (
//       typeof(body.expenseID) === 'undefined'
//       || typeof(body.amount) === 'undefined'
//       || typeof(body.title) === 'undefined'
//       || typeof(body.comment) === 'undefined'
//       || typeof(body.groupID) === 'undefined'
//       || typeof(body.userID) === 'undefined'
//       || typeof(body.datetime) === 'undefined'
//     ) {
//       return false;
//     }
//     return true;
//   },

//   expense: function(db, request, response, body, callback) {
//     let pathArray = request.url.split('/');
//     let userID = parseInt(pathArray[pathArray.length - 1]);
//     if (request.method === 'POST') {
//       if (!api._verifyExpenseBody(body)) {
//         callback(Error.ErrorCodes.MALFORMED_REQUEST);
//       } else {
//         let t = new Expense.Expense();
//         t.useDB(db);
//         t.amount = body.amount;
//         t.title = body.title;
//         t.comment = body.comment;
//         t.groupID = body.groupID;
//         t.userID = body.userID;
//         t.save(request.headers['fairsplit-apikey'], (res) => {
//           callback(res);
//         });
//       }
//     } else if (request.method === 'GET') {
//       if (pathArray.indexOf('byUserID') > -1) {
//         if (userID > 0) {
//           Expense.Expense.getAllByUserID(db, userID, (res) => {
//             callback(res);
//           });
//         } else {
//           callback(Error.ErrorCodes.MALFORMED_REQUEST);
//         }
//       }
//     } else if (request.method === 'PUT') {
//       if (!api._verifyExpenseBody(body)) {
//         callback(Error.ErrorCodes.MALFORMED_REQUEST);
//       } else {
//         let t = new Expense.Expense();
//         t.useDB(db);
//         t.expenseID = body.expenseID;
//         t.amount = body.amount;
//         t.title = body.title;
//         t.comment = body.comment;
//         t.groupID = body.groupID;
//         t.userID = body.userID;
//         t.save(request.headers['fairsplit-apikey'], (res) => {
//           callback(res);
//         });
//       }
//     } else if (request.method === 'DELETE') {
//       if (
//         typeof(userID) !== 'number'
//         || userID <= 0
//       ) {
//         callback(Error.ErrorCodes.MALFORMED_REQUEST);
//       } else {
//         Expense.Expense.deleteByID(db, userID, (res) => {
//           callback(res);
//         });
//       }
//     }
//   }
// };

// module.exports = {
//   handle: api.handle
// };
