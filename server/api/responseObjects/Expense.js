let BaseResponse = require('./BaseResponse.js');

class ExpenseResponse extends BaseResponse {
  constructor() {
    super();
    this.type = 'ExpenseResponse';
  }
}

class Expense {
  constructor() {
    this.type = 'Expense';
    this.expenseID = 0;
    this.amount = 0;
    this.title = '';
    this.comment = '';
    this.groupID = 0;
    this.userID = 0;
    this.datetime = 0;
    this.db = null;
  }

  _validateAPIKeyAgainstOwner(apiKey, callback) {
    let that = this;
    this.db.get(
      'SELECT userID FROM users WHERE apiKey=?',
      [apiKey],
      function(err, row) {
        if (err) {
          callback(false);
        } else {
          if (row != null) {
            if (row.userID === that.userID) {
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

  save(apiKey, callback) {
    let that = this;
    this._validateAPIKeyAgainstOwner(apiKey, (valid) => {
      if (valid) {
        if (that.expenseID > 0) {
          that.db.run(
            'UPDATE expenses SET amount=?, title=?, comment=? WHERE expenseID=?',
            [that.amount, that.title, that.comment, that.expenseID],
            function(err) {
              if (err) {
                callback(err);
              } else {
                callback(that);
              }
            }
          );
        } else {
          that.datetime = Math.floor(Date.now() / 1000);
          that.db.run(
            'INSERT INTO expenses (amount, title, comment, groupID, userID, datetime) VALUES (?, ?, ?, ?, ?, ?)',
            [that.amount, that.title, that.comment, that.groupID, that.userID, that.datetime],
            function(err) {
              if (err) {
                callback(err.message);
              } else {
                that.expenseID = this.lastID;
                callback(that);
              }
            }
          );
        }
      } else {
        callback(false);
      }
    });
  }

  getByID(expenseID, callback) {
    let that = this;
    this.db.get(
      'SELECT * FROM expenses WHERE expenseID=?',
      [expenseID],
      function(err, row) {
        if (err) {
          callback(err);
        } else {
          that.expenseID = row.expenseID;
          that.amount = row.amount;
          that.title = row.title;
          that.comment = row.comment;
          that.groupID = row.groupID;
          that.userID = row.userID;
          that.datetime = row.datetime;
          callback(that);
        }
      }
    );
  }

  static deleteByID(db, expenseID, callback) {
    db.run(
      'DELETE FROM expenses WHERE expenseID=?',
      [expenseID],
      function(err) {
        if (err) {
          callback(err);
        } else {
          callback(true);
        }
      }
    );
  }

  static getAllByUserID(db, userID, callback) {
    db.all(
      'SELECT * FROM expenses WHERE userID=?',
      [userID],
      function(err, rows) {
        if (err) {
          callback(err);
        } else {
          let expenses = [];
          rows.forEach((row) => {
            let t = new Expense();
            t.expenseID = row.expenseID;
            t.amount = row.amount;
            t.title = row.title;
            t.comment = row.comment;
            t.groupID = row.groupID;
            t.userID = row.userID;
            t.datetime = row.datetime;
            expenses.push(t);
          });
          callback(expenses);
        }
      }
    );
  }
}

module.exports = {
  'ExpenseResponse': ExpenseResponse,
  'Expense': Expense
}
