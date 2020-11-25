const BaseResponse = require('./base-response.class.js');

class GroupResponse extends BaseResponse {
  constructor() {
    super();
    this.type = 'GroupResponse';
  }
}

class GroupResponseData {
  constructor() {
    this.type = 'GroupResponseData';
    this.groupID = 0;
    this.groupName = '';
    this.owner = 0;
    this.members = [];
  }
}

module.exports = {
  'GroupResponse': GroupResponse,
  'GroupResponseData': GroupResponseData
};
