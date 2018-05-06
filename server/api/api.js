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
    console.log("======== START RESPONSE ========");
    console.log(JSON.stringify(data));
    console.log("======== END RESPONSE BODY ========");
    // END DEBUG
  },

  handle: function(db, request, response) {
    let handler = url.parse(request.url.substring('/api/'.length)).pathname;

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
      console.log("======== START REQUEST ========");
      console.log(request.method, request.url);
      console.log("======== HEADERS ========");
      console.log(request.headers);
      console.log("======== BODY ========");
      console.log(JSON.stringify(body));
      console.log("======== END REQUEST ========");
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
            api.group(db, request, response, body);
          } else if (handler === 'transaction') {
            api.transaction(db, request, response, body);
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
      if (body.password != body.passwordAgain) {
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

  user: function(db, request, response, body) {
    let u = new User.User();
    u.useDB(db);
    if (u) {
      let r = new User.UserResponse();
      r.requestUri = request.url;
      r.data.push(u);

      response.writeHead(200, {'Content-Type': 'application/json'});
      response.write(JSON.stringify(r));
      response.end();
      return true;
    } else {
      return 'User not found or password mismatch.';
    }
  },

  group: function(db, request, response, body) {
    let g = new Group.Group();
    g.useDB(db);
    if (g) {
      let r = new Group.GroupResponse();
      r.requestUri = request.url;

      response.writeHead(200, {'Content-Type': 'application/json'});
      response.write(JSON.stringify(r));
      response.end();
      return true;
    } else {
      return 'User not found or password mismatch.';
    }
  },

  transaction: function(db, request, response, body) {
    if (request.method === 'POST') {
      let t = new Transaction.Transaction(body.amount, body.comment);
      t.useDB(db);
      if (t) {
        let r = new Transaction.TransactionResponse();
        r.requestUri = request.url;

        response.writeHead(200, {'Content-Type': 'application/json'});
        response.write(JSON.stringify(r));
        response.end();
        return true;
      } else {
        return 'User not found or password mismatch.';
      }
    }
  }
};

module.exports = {
  handle: api.handle
};
