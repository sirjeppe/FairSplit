let BaseResponse = require('./BaseResponse.js');

class GroupResponse extends BaseResponse {
  constructor() {
    super();
    this.type = 'GroupResponse';
  }
}

class Group {
  constructor() {
    this.type = 'Group';
    this.groupID = 0;
    this.groupName = '';
    this.owner = 0;
    this.members = [];
    this.db = null;
  }

  _validateAPIKeyAgainstOwner(apiKey, callback) {
    this.db.get(
      'SELECT userID FROM users WHERE apiKey = ?',
      [apiKey],
      function(err, row) {
        if (err) {
          callback(false);
        } else {
          if (row != null) {
            if (row.userID === this.owner) {
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

  useDB(db) {
    this.db = db;
  }

  getByID(groupID, callback) {
    let that = this;
    this.db.get(
      'SELECT * FROM groups WHERE groupID=?',
      [groupID],
      function(err, row) {
        if (err) {
          callback(err);
        } else {
          that.groupID = row.groupID;
          that.groupName = row.groupName;
          that.owner = row.owner;
          that.members = (row.members) ? row.members.split(',').map((n) => { return parseInt(n); }) : [];
          callback(that);
        }
      }
    );
  }

  save(apiKey, callback) {
    _validateAPIKeyAgainstOwner(apiKey, (valid) => {
      if (valid) {
        this.db.run(
          'UPDATE groups SET groupName = ?, members = ? WHERE groupID = ?'
          [this.groupName.trim(), this.members.join(','), this.groupID],
          function(err) {
            if (err) {
              callback(false);
            } else {
              callback(true);
            }
          }
        )
      } else {
        callback(false);
      }
    });
  }
}

module.exports = {
  'GroupResponse': GroupResponse,
  'Group': Group
}
