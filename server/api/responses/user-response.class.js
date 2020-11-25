const BaseResponse = require('./base-response.class.js');

class UserResponse extends BaseResponse {
  constructor() {
    super();
    this.type = 'UserResponse';
  }
}

class UserResponseData {
  constructor() {
    this.type = 'UserResponseData';
    this.userID = 0;
    this.userName = '';
    this.income = 0;
    this.groups = [];
    this.apiKey = '';
    this.keyValidTo = 0;
  }
}

module.exports = {
  'UserResponse': UserResponse,
  'UserResponseData': UserResponseData
};