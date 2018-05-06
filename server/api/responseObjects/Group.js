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
    this.members = [];
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
          that.members = (row.members) ? row.members.split(',').map((n) => { return parseInt(n); }) : [];
          callback(that);
        }
      }
    );
  }
}

module.exports = {
  'GroupResponse': GroupResponse,
  'Group': Group
}
