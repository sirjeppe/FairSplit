const BackendModule = require('./backend-module.class.js');
const ur = require('./responses/user-response.class.js');
const crypto = require('crypto');
const config = require('../config.js');

class User extends BackendModule {
  constructor(api) {
    super(api);
    this.apiEndPoint = '/user';
  }

  _generateAPIKey() {
    let apiKey = '';
    let allowedChars = '0123456789_-=.abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ';
    for (let i = 0; i < 32; i++) {
      apiKey += allowedChars.charAt(Math.floor(Math.random() * allowedChars.length));
    }
    return apiKey;
  }

  restoreSession(request, response, userID) {
    let cls = this;
    if (!request.headers.hasOwnProperty('fairsplit-apikey')) {
      this.respond(request, response, 'fairsplit-apikey header missing');
      return;
    }
    let apiKey = request.headers['fairsplit-apikey'];
    let now = Math.floor(Date.now() / 1000);
    this.api.db.get(
      'SELECT * FROM users WHERE apiKey=? AND userID=? AND keyValidTo >= ?',
      [apiKey, userID, now],
      function(err, row) {
        if (err || row === undefined) {
          cls.respond(request, response, 'Failed to validate the API key!');
        } else {
          // For each successful request, prolong validity by 2 weeks
          let twoWeeks = 60 * 60 * 24 * 14;
          let newValidTime = parseInt(now + twoWeeks);
          cls.api.db.run(
            'UPDATE users SET keyValidTo=? WHERE userID=?',
            [newValidTime, row.userID],
            function(err) {
              if (err) {
                console.error('Failed to set new valid time', err, 'for userID', row.userID);
              }
            }
          );
          let uResData = new ur.UserResponseData();
          uResData.userID = row.userID;
          uResData.userName = row.userName;
          uResData.income = row.income;
          uResData.groups = (row.groups) ? row.groups.split(',').map((n) => { return parseInt(n); }) : [];
          // ... But don't forget apiKey if request in apiKey matches row.apiKey
          // (required for successive API calls from app)
          if (apiKey == row.apiKey) {
            uResData.apiKey = row.apiKey;
          }
          let responseObject = new ur.UserResponse();
          responseObject.data.push(uResData);
          cls.respond(request, response, responseObject);
        }
      }
    );
  }

  login(request, response, body) {
    let reqProps = ['userName', 'password'];
    if (this.requireBodyElements(request, response, body, reqProps)) {
      let cls = this;
      let userName = body.userName.trim();
      let password = body.password;
      this.api.db.get('SELECT * FROM users WHERE userName=?', [userName], function(err, row) {
        if (err) {
          cls.respond(request, response, err);
        } else if (!row) {
          cls.respond(request, response, 'User not found!');
        } else {
          let hash = crypto.createHash('sha256');
          hash.update(password);
          if (hash.digest('hex') === row.password) {
            let uResData = new ur.UserResponseData();
            uResData.userID = row.userID;
            uResData.userName = row.userName;
            uResData.income = row.income;
            uResData.groups = (row.groups) ? row.groups.split(',').map((n) => { return parseInt(n); }) : [];
            // Update API key
            let apiKey = cls._generateAPIKey();
            let now = Math.floor(Date.now() / 1000);
            let expires = now + 60 * config['keyValidMinutes'];
            cls.api.db.run(
              'UPDATE users SET apiKey=?, keyValidTo=? WHERE userID=?',
              [apiKey, expires, row.userID],
              function(err) {
                if (err) {
                  cls.respond(request, response, err);
                } else {
                  uResData.apiKey = apiKey;
                  uResData.keyValidTo = expires;
                  let responseObject = new ur.UserResponse();
                  responseObject.data.push(uResData)
                  cls.respond(request, response, responseObject);
                }
              }
            );
          } else {
            cls.respond(request, response, 'Wrong user name or password!');
          }
        }
      });
    }
  }

  register(request, response, body) {
    if (this.requireBodyElements(request, response, body, ['userName', 'password'])) {
      let cls = this;
      let uResData = new ur.UserResponseData();
      let userName = body.userName.trim();
      let hash = crypto.createHash('sha256');
      hash.update(body.password);
      let apiKey = this._generateAPIKey();
      let now = Math.floor(Date.now() / 1000);
      let expires = now + 60 * config['keyValidMinutes'];
      this.api.db.run(
        'INSERT INTO users (userName, password, apiKey, keyValidTo) VALUES (?, ?, ?, ?)',
        [userName, hash.digest('hex'), apiKey, expires],
        function(err) {
          if (err) {
            cls.respond(request, response, err);
          } else {
            uResData.userID = this.lastID;
            uResData.userName = userName;
            uResData.apiKey = apiKey;
            uResData.keyValidTo = expires;
            cls.api.db.run(
              'INSERT INTO groups (groupName, owner, members) VALUES (?, ?, ?)',
              ['My first group', uResData.userID, uResData.userID],
              function(err) {
                if (err) {
                  cls.respond(request, response, err);
                } else {
                  uResData.groups.push(this.lastID);
                  cls.api.db.run(
                    'UPDATE users SET groups=? WHERE userID=?',
                    [uResData.groups.join(','), uResData.userID],
                    function(err) {
                      if (err) {
                        cls.respond(request, response, err);
                      } else {
                        cls.respond(request, response, uResData);
                      }
                    }
                  );
                }
              }
            );
          }
        }
      );
    }
  }

  save(request, response, body) {
    if (this.requireBodyElements(request, response, body, ['userID', 'income'])) {
      let cls = this;
      this.api.db.run(
        'UPDATE users SET income=? WHERE userID=?',
        [body.income, body.userID],
        function(err) {
          if (err) {
            cls.respond(request, response, err);
          } else {
            cls.respond(request, response, true);
          }
        }
      );
    }
  }

  internalRoute(request, response, body) {
    if (request.method === 'GET') {
      let userID = parseInt(request.url.split('/').pop());
      if (!isNaN(userID)) {
        this.restoreSession(request, response, userID);
      }
    } else if (request.method === 'POST') {
      if (request.url.endsWith('/login')) {
        this.login(request, response, body);
      } else if (request.url.endsWith('/register')) {
        this.register(request, response, body);
      }
    }
    
    this._validateAPIKeyAgainstUserID(request, body, valid => {
      if (valid) {
        if (request.method === 'PUT') {
          this.save(request, response, body);
        }
      }
    });
  }
}

module.exports = {
  'User': User
};
