let BaseResponse = require('./BaseResponse.js');

class TransactionResponse extends BaseResponse {
  constructor() {
    super();
    this.type = 'TransactionResponse';
  }
}

class Transaction {
  constructor(amount, comment, groupID, userID) {
    this.type = 'Transaction';
    this.amount = 0;
    this.comment = '';
    this.groupID = 0;
    this.userID = 0;
    this.db = null;
  }

  useDB(db) {
    this.db = db;
  }
}

module.exports = {
  'TransactionResponse': TransactionResponse,
  'Transaction': Transaction
}
