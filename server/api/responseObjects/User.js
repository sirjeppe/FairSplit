const BaseResponse = require('./BaseResponse.js');
const Group = require('./Group.js');
const process = require('process');
const crypto = require('crypto');
const config = require('../../config.js');

class UserResponse extends BaseResponse {
  constructor() {
    super();
    this.type = 'UserResponse';
  }
}

class User {
  constructor() {
    this.type = 'User';
    this.userID = 0;
    this.userName = '';
    this.salary = 0;
    this.groups = [];
    this.apiKey = '';
    this.keyValidTo = 0;
    this.db = null;
  }

  _generateAPIKey() {
    let apiKey = '';
    let allowedChars = '0123456789_-=.abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ';
    for (let i = 0; i < 32; i++) {
      apiKey += allowedChars.charAt(Math.floor(Math.random() * allowedChars.length));
    }
    return apiKey;
  }

  useDB(db) {
    this.db = db;
  }

  login(userName, password, callback) {
    let that = this;
    this.db.get('SELECT * FROM users WHERE userName=?', [userName], function(err, row) {
      if (err) {
        callback(err);
      } else if (!row) {
        callback('User not found!');
      } else {
        let hash = crypto.createHash('sha256');
        hash.update(password);
        if (hash.digest('hex') === row.password) {
          that.userID = row.userID;
          that.userName = row.userName;
          that.salary = row.salary;
          that.groups = (row.groups) ? row.groups.split(',').map((n) => { return parseInt(n); }) : [];
          // Update API key
          let apiKey = that._generateAPIKey();
          let now = Math.floor(Date.now() / 1000);
          let expires = now + 60 * config['keyValidMinutes'];
          that.db.run(
            'UPDATE users SET apiKey=?, keyValidTo=? WHERE userID=?',
            [apiKey, expires, row.userID],
            function(err) {
              if (err) {
                callback(err);
              } else {
                that.apiKey = apiKey;
                that.keyValidTo = expires;
                callback(that);
              }
            }
          );
        } else {
          callback('Wrong user name or password!');
        }
      }
    });
  }

  register(userName, password, callback) {
    let that = this;
    let hash = crypto.createHash('sha256');
    hash.update(password);
    let apiKey = this._generateAPIKey();
    let now = Math.floor(Date.now() / 1000);
    let expires = now + 60 * config['keyValidMinutes'];
    this.db.run(
      'INSERT INTO users (userName, password, apiKey, keyValidTo) VALUES (?, ?, ?, ?)',
      [userName, hash.digest('hex'), apiKey, expires],
      function(err) {
        if (err) {
          callback(err);
        } else {
          that.userID = this.lastID;
          that.userName = userName;
          that.apiKey = apiKey;
          that.keyValidTo = expires;
          that.db.run(
            'INSERT INTO groups (groupName, members) VALUES (?, ?)',
            ['My first group', that.userID],
            function(err) {
              if (err) {
                callback(err);
              } else {
                that.groups.push(this.lastID);
                that.db.run(
                  'UPDATE users SET groups=? WHERE userID=?',
                  [that.groups.join(','), that.userID],
                  function(err) {
                    if (err) {
                      callback(err);
                    } else {
                      callback(that);
                    }
                  }
                );
              }
            }
          )

        }
      }
    );
  }
}

module.exports = {
  'UserResponse': UserResponse,
  'User': User
}
