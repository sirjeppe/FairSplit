const BaseResponse = require('./responses/base-response.class');
const Error = require('./responses/error.class.js');

class BackendModule {
  constructor(api) {
    this.api = api;
    this.debugPrintouts = true;
  }

  _validateAPIKeyAgainstUserID(request, body, callback) {
    let cls = this;
    if (!request.headers.hasOwnProperty('fairsplit-apikey')) {
      callback('fairsplit-apikey header missing');
      return;
    }
    if (!body.hasOwnProperty('userID')) {
      callback('userID property missing');
      return;
    }
    this.api.db.get(
      'SELECT userID FROM users WHERE apiKey=?',
      [request.headers['fairsplit-apikey']],
      function(err, row) {
        if (err) {
          callback(false);
        } else {
          if (row != null) {
            if (row.userID === body.userID) {
              callback(true);
            } else {
              callback(false);
            }
          } else {
            callback(false);
          }
        }
      }
    );
  }
  
  parseRequest(request, response, callback) {
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
      if (this.debugPrintouts) {
        console.log();
        console.log('======== START REQUEST ========');
        console.log(request.method, request.url);
        console.log('======== HEADERS ========');
        console.log(request.headers);
        if (request.method === 'POST' || request.method === 'PUT' || request.method === 'DELETE') {
          console.log('======== BODY ========');
          let printBody = Object.assign({}, body);
          if (printBody.hasOwnProperty('password')) {
            delete printBody.password;
          }
          if (printBody.hasOwnProperty('passwordAgain')) {
            delete printBody.passwordAgain;
          }
          console.log(JSON.stringify(printBody));
        }
        console.log('======== END REQUEST ========');
      }
      // END DEBUG

      callback(request, response, body);
    });
  }

  respond(request, response, data) {
    let code = 200;
    let temp = data;
    if (typeof(data) === 'string') {
      data = new Error.ErrorResponse();
      data.requestUri = request.url;
      data.message = temp;
      data.errorCode = 100; // Uknown error
      code = 400;
    } else if (typeof(data) === 'number') {
      data = new Error.ErrorResponse();
      data.requestUri = request.url;
      data.errorCode = temp;
      data.message = Error.ErrorMessages[temp];
      code = 400;
    } else if (typeof(data) === 'boolean') {
      data = new BaseResponse();
      data.requestUri = request.url;
      data.errorCode = (temp) ? 0 : Error.ErrorCodes.COMMAND_FAILED;
      data.message = (temp) ? 'Command successful' : Error.ErrorMessages[data.errorCode];
      code = (temp) ? code : 400;
    } else if (data instanceof BaseResponse) {
      if (data instanceof Error.ErrorResponse) {
        code = 400;
      }
    }
    response.writeHead(code, {'Content-Type': 'application/json'});
    response.write(JSON.stringify(data));
    response.end();

    // DEBUG
    if (this.debugPrintouts) {
      console.log();
      console.log('======== START RESPONSE ========');
      console.log(JSON.stringify(data));
      console.log('======== END RESPONSE BODY ========');
    }
    // END DEBUG
  }

  handle(request, response) {
    let cls = this;
    this.parseRequest(request, response, (req, res, body) => {
      cls.internalRoute(req, res, body);
    });
  }

  requireBodyElements(request, response, body, elementArray) {
    for (let elm of elementArray) {
      if (!body.hasOwnProperty(elm)) {
        this.respond(request, response, Error.ErrorCodes.MALFORMED_REQUEST);
        return false;
      }
    }
    return true;
  }
}

module.exports = BackendModule;
