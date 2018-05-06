const url = require('url');
const fs = require('fs');
let responses = [
  './responseObjects/BaseResponse.js',
  './responseObjects/Error.js',
  './responseObjects/User.js',
  './responseObjects/Group.js',
  './responseObjects/Transaction.js'
];
for (let i = 0; i < responses.length; i++) {
  delete require.cache[require.resolve(responses[i])];
}
let BaseResponse = require(responses[0]);
let Error = require(responses[1]);
let User = require(responses[2]);
let Group = require(responses[3]);
let Transaction = require(responses[4]);

let api = {
  respond: function(request, response, data) {
    let code = 200;
    if (data instanceof Error.ErrorResponse || typeof(data) === 'string') {
      code = 400;
    }
    let temp = data;
    if (typeof(data) === 'string') {
      data = new Error.ErrorResponse();
      data.requestUri = request.url;
      data.message = temp;
      data.errorCode = 100; // Uknown error
    } else if (typeof(data) === 'number') {
      data = new Error.ErrorResponse();
      data.requestUri = request.url;
      data.errorCode = temp;
      data.message = Error.ErrorMessages[temp];
    } else if (data instanceof BaseResponse) {
      // Strip out db property from data objects if there are any before sending
      // back the response
      for (let i = 0; i < data.data.length; i++) {
        if ('db' in data.data[i]) {
          delete data.data[i].db;
        }
      }
    }
    response.writeHead(code, {'Content-Type': 'application/json'});
    response.write(JSON.stringify(data));
    response.end();

    // DEBUG
    console.log();
    console.log('======== START RESPONSE ========');
    console.log(JSON.stringify(data));
    console.log('======== END RESPONSE BODY ========');
    // END DEBUG
  },

  handle: function(db, request, response) {
    let handler = url.parse(request.url.substring('/api/'.length)).pathname.split('/')[0];

    let body = {};
    let bodyArray = [];
    request.on('data', (chunk) => {
      bodyArray.push(chunk);
    }).on('end', () => {
      let bodyString = Buffer.concat(bodyArray).toString();
      try {
        body = JSON.parse(bodyString);
      } catch (err) {
        body = {};
      }

      // DEBUG
      console.log();
      console.log('======== START REQUEST ========');
      console.log(request.method, request.url);
      console.log('======== HEADERS ========');
      console.log(request.headers);
      if (request.method !== 'GET') {
        console.log('======== BODY ========');
        console.log(JSON.stringify(body));
      }
      console.log('======== END REQUEST ========');
      // END DEBUG

      // Validate API key
      api.validateApiKey(db, request, (validationResponse) => {
        if (typeof(validationResponse) === 'string') {
          if (handler === 'login') {
            api.login(db, request, response, body, (res) => {
              if (res instanceof User.User) {
                let r = new User.UserResponse();
                r.requestUri = request.url;
                r.data.push(res);
                api.respond(request, response, r);
              } else {
                api.respond(request, response, res);
              }
            });
          } else if (handler === 'register') {
            api.register(db, request, response, body, (res) => {
              if (res instanceof User.User) {
                let r = new User.UserResponse();
                r.requestUri = request.url;
                r.data.push(res);
                api.respond(request, response, r);
              } else {
                api.respond(request, response, res);
              }
            });
          } else {
            api.respond(request, response, validationResponse);
          }
        } else {
          if (handler === 'user') {
            api.user(db, request, response, body);
          } else if (handler === 'group') {
            api.group(db, request, response, body, (res) => {
              if (res instanceof Group.Group) {
                let r = new Group.GroupResponse();
                r.requestUri = request.url;
                r.data.push(res);
                api.respond(request, response, r);
              } else {
                api.respond(request, response, res);
              }
            });
          } else if (handler === 'transaction') {
            api.transaction(db, request, response, body, (res) => {
              if (res instanceof Array) {
                let r = new Transaction.TransactionResponse();
                r.requestUri = request.url;
                r.data = res;
                api.respond(request, response, r);
              } else if (res instanceof Transaction.Transaction) {
                let r = new Transaction.TransactionResponse();
                r.requestUri = request.url;
                r.data.push(res);
                api.respond(request, response, r);
              } else {
                api.respond(request, response, res);
              }
            });
          } else {
            api.respond(request, response, 'No handler found for request: ' + request.url);
          }
        }
      });
    });
  },

  validateApiKey: function(db, request, callback) {
    if (!('fairsplit-apikey' in request.headers)) {
      callback('fairsplit-apikey header missing');
      return;
    }
    let now = Math.floor(Date.now() / 1000);
    db.get(
      'SELECT * FROM users WHERE apiKey = ? AND keyValidTo >= ?',
      [request.headers['fairsplit-apikey'], now],
      (err, row) => {
        if (err) {
          callback('Failed to validate the API key!');
        } else {
          callback(true);
        }
      }
    );
  },

  login: function(db, request, response, body, callback) {
    if (typeof(body.userName) === 'undefined' || typeof(body.password) === 'undefined') {
      callback(Error.ErrorCodes.NO_BODY);
    } else {
      let u = new User.User();
      u.useDB(db);
      u.login(body.userName, body.password, (res) => {
        callback(res);
      });
    }
  },

  register: function(db, request, response, body, callback) {
    if (typeof(body.userName) === 'undefined' || typeof(body.password) === 'undefined' || typeof(body.passwordAgain) === 'undefined') {
      callback(Error.ErrorCodes.MALFORMED_REQUEST);
    } else {
      if (body.password !== body.passwordAgain) {
        callback(Error.ErrorCodes.PASSWORDS_MISMATCH);
        return;
      }
      let u = new User.User();
      u.useDB(db);
      u.register(body.userName, body.password, (res) => {
        callback(res);
      });
    }
  },

  user: function(db, request, response, body, callback) {
    if (request.method === 'GET') {
      let pathArray = request.url.split('/');
      let userID = parseInt(pathArray[pathArray.length - 1]);
      if (userID > 0) {
        let u = new User.User();
        u.useDB(db);
        u.getByID(userID, (res) => {
          callback(res);
        });
      } else {
        callback(Error.ErrorCodes.MALFORMED_REQUEST);
      }
    }
  },

  group: function(db, request, response, body, callback) {
    if (request.method === 'GET') {
      let pathArray = request.url.split('/');
      let groupID = parseInt(pathArray[pathArray.length - 1]);
      if (groupID > 0) {
        let g = new Group.Group();
        g.useDB(db);
        g.getByID(groupID, (res) => {
          callback(res);
        });
      } else {
        callback(Error.ErrorCodes.MALFORMED_REQUEST);
      }
    }
  },

  transaction: function(db, request, response, body, callback) {
    if (request.method === 'POST') {
      if (typeof(body.amount) === 'undefined' || typeof(body.comment) === 'undefined' || typeof(body.groupID) === 'undefined' || typeof(body.userID) === 'undefined') {
        callback(Error.ErrorCodes.MALFORMED_REQUEST);
      } else {
        let t = new Transaction.Transaction();
        t.useDB(db);
        t.amount = body.amount;
        t.title = body.title;
        t.comment = body.comment;
        t.groupID = body.groupID;
        t.userID = body.userID;
        t.save((res) => {
          callback(res);
        });
      }
    } else if (request.method === 'GET') {
      let pathArray = request.url.split('/');
      if (pathArray.indexOf('byUserID') > -1) {
        let userID = parseInt(pathArray[pathArray.length - 1]);
        if (userID > 0) {
          Transaction.getAllByUserID(db, userID, (res) => {
            callback(res);
          });
        } else {
          callback(Error.ErrorCodes.MALFORMED_REQUEST);
        }
      }
    }
  }
};

module.exports = {
  handle: api.handle
};
