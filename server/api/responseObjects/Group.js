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
}

module.exports = {
  'GroupResponse': GroupResponse,
  'Group': Group
}
