let BaseResponse = require('./BaseResponse.js');

class TransactionResponse extends BaseResponse {
  constructor() {
    super();
    this.type = 'TransactionResponse';
  }
}

class Transaction {
  constructor(groupID, userID, amount, title, comment) {
    this.type = 'Transaction';
    this.transactionID = -1;
    this.amount = amount || 0;
    this.title = title || '';
    this.comment = comment || '';
    this.groupID = groupID || 0;
    this.userID = userID || 0;
    this.datetime = 0;
    this.db = null;
  }

  useDB(db) {
    this.db = db;
  }

  save(callback) {
    let that = this;
    this.datetime = Math.floor(Date.now() / 1000);
    this.db.run(
      'INSERT INTO transactions (groupID, userId, amount, title, comment, datetime) VALUES (?, ?, ?, ?, ?, ?)',
      [this.groupID, this.userID, this.amount, this.title, this.comment, this.datetime],
      function(err) {
        if (err) {
          callback(err);
        } else {
          that.transactionID = this.lastID;
          callback(that);
        }
      }
    );
  }
}

module.exports = {
  'TransactionResponse': TransactionResponse,
  'Transaction': Transaction
}
