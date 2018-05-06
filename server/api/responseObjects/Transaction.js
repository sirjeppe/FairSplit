let BaseResponse = require('./BaseResponse.js');

class TransactionResponse extends BaseResponse {
  constructor() {
    super();
    this.type = 'TransactionResponse';
  }
}

class Transaction {
  constructor() {
    this.type = 'Transaction';
    this.transactionID = 0;
    this.amount = 0;
    this.title = '';
    this.comment = '';
    this.groupID = 0;
    this.userID = 0;
    this.db = null;
  }

  useDB(db) {
    this.db = db;
  }

  save(callback) {
    let that = this;
    if (this.transactionID > 0) {
      this.db.run(
        'UPDATE transactions SET amount=?, title=?, comment=? WHERE transactionID=?',
        [this.amount, this.title, this.comment, this.transactionID],
        function(err) {
          if (err) {
            callback(err);
          } else {
            callback(that);
          }
        }
      );
    } else {
      let now = Math.floor(Date.now() / 1000);
      this.db.run(
        'INSERT INTO transactions (amount, title, comment, groupID, userID, datetime) VALUES (?, ?, ?, ?, ?, ?)',
        [this.amount, this.title, this.comment, this.groupID, this.userID, now],
        function(err) {
          if (err) {
            callback(err.message);
          } else {
            that.transactionID = this.lastID;
            callback(that);
          }
        }
      );
    }
  }

  getByID(transactionID, callback) {
    let that = this;
    this.db.get(
      'SELECT * FROM transactions WHERE transactionID=?',
      [transactionID],
      function(err, row) {
        if (err) {
          callback(err);
        } else {
          that.transactionID = row.transactionID;
          that.amount = row.amount;
          that.title = row.title;
          that.comment = row.comment;
          that.groupID = row.groupID;
          that.userID = row.userID;
          callback(that);
        }
      }
    );
  }

  static getAllByUserID(db, userID, callback) {
    db.all(
      'SELECT * FROM transactions WHERE userID=?',
      [userID],
      function(err, rows) {
        if (err) {
          callback(err);
        } else {
          let transactions = [];
          rows.forEach((row) => {
            let t = new Transaction();
            t.transactionID = row.transactionID;
            t.amount = row.amount;
            t.title = row.title;
            t.comment = row.comment;
            t.groupID = row.groupID;
            t.userID = row.userID;
            transactions.push(t);
          });
          callback(transactions);
        }
      }
    );
  }
}

module.exports = {
  'TransactionResponse': TransactionResponse,
  'Transaction': Transaction
}
