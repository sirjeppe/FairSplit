const BackendModule = require('./backend-module.class.js');
const er = require('./responses/expense-response.class.js');
const Error = require('./responses/error.class.js');

class Expense extends BackendModule {
  constructor(api) {
    super(api);
    this.apiEndPoint = '/expense';
  }

  save(request, response, body) {
    let responseObject = new er.ExpenseResponse();
    let eResData = Object.assign(new er.ExpenseResponseData, body);
    responseObject.data.push(eResData);
    let cls = this;
    if (eResData.expenseID > 0) {
      cls.api.db.run(
        'UPDATE expenses SET amount=?, title=?, comment=? WHERE expenseID=?',
        [eResData.amount, eResData.title, eResData.comment, eResData.expenseID],
        function(err) {
          if (err) {
            cls.respond(request, response, err);
          } else {
            cls.respond(request, response, responseObject);
          }
        }
      );
    } else {
      eResData.datetime = Math.floor(Date.now() / 1000);
      cls.api.db.run(
        'INSERT INTO expenses (amount, title, comment, groupID, userID, datetime) VALUES (?, ?, ?, ?, ?, ?)',
        [eResData.amount, eResData.title, eResData.comment, eResData.groupID, eResData.userID, eResData.datetime],
        function(err) {
          if (err) {
            cls.respond(request, response, err.message);
          } else {
            eResData.expenseID = this.lastID;
            cls.respond(request, response, responseObject);
          }
        }
      );
    }
  }

  getByID(request, response, expenseID) {
    let cls = this;
    this.db.get(
      'SELECT * FROM expenses WHERE expenseID=?',
      [expenseID],
      function(err, row) {
        if (err) {
          cls.respond(request, response, err);
        } else {
          let eResData = new er.ExpenseResponseData();
          eResData.expenseID = row.expenseID;
          eResData.amount = row.amount;
          eResData.title = row.title;
          eResData.comment = row.comment;
          eResData.groupID = row.groupID;
          eResData.userID = row.userID;
          eResData.datetime = row.datetime;
          let responseObject = new er.ExpenseResponse();
          responseObject.data.push(eResData);
          cls.respond(request, response, responseObject);
        }
      }
    );
  }

  deleteByID(request, response, body) {
    if (this.requireBodyElements(request, response, body, ['expenseID'])) {
      let cls = this;
      this.api.db.run(
        'DELETE FROM expenses WHERE expenseID=?',
        [body.expenseID],
        function(err) {
          if (err) {
            cls.respond(request, response, err);
          } else {
            cls.respond(request, response, true);
          }
        }
      );
    }
  }

  getAllByUserID(request, response, userID) {
    let cls = this;
    this.api.db.all(
      'SELECT * FROM expenses WHERE userID=? ORDER BY datetime DESC',
      [userID],
      function(err, rows) {
        if (err) {
          cls.respond(request, response, err);
        } else {
          let responseObject = new er.ExpenseResponse();
          rows.forEach((row) => {
            let eResData = new er.ExpenseResponseData();
            eResData.expenseID = row.expenseID;
            eResData.amount = row.amount;
            eResData.title = row.title;
            eResData.comment = row.comment;
            eResData.groupID = row.groupID;
            eResData.userID = row.userID;
            eResData.datetime = row.datetime;
            responseObject.data.push(eResData);
          });
          cls.respond(request, response, responseObject);
        }
      }
    );
  }
  
  internalRoute(request, response, body) {
    let cls = this;
    this._validateAPIKeyAgainstUserID(request, body, valid => {
      if (valid) {
        if (request.method === 'GET') {
          if (request.url.indexOf('/byUserID/') > -1) {
            let userID = parseInt(request.url.split('/').pop());
            cls.getAllByUserID(request, response, userID);
          }
        } else if (request.method === 'POST') {
          cls.save(request, response, body);
        } else if (request.method === 'PUT') {
          cls.save(request, response, body);
        } else if (request.method === 'DELETE') {
          cls.deleteByID(request, response, body);
        }
      } else {
        cls.respond(request, response, Error.ErrorCodes.MALFORMED_REQUEST);
      }
    });
  }
}

module.exports = {
  'Expense': Expense
};
