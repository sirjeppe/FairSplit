const BaseResponse = require('./base-response.class.js');

class ExpenseResponse extends BaseResponse {
  constructor() {
    super();
    this.type = 'ExpenseResponse';
  }
}

class ExpenseResponseData {
  constructor() {
    this.type = 'ExpenseResponseData';
    this.expenseID = 0;
    this.amount = 0;
    this.title = '';
    this.comment = '';
    this.groupID = 0;
    this.userID = 0;
    this.datetime = 0;
  }
}

module.exports = {
  'ExpenseResponse': ExpenseResponse,
  'ExpenseResponseData': ExpenseResponseData
};
