const BackendModule = require('./backend-module.class.js');
const gr = require('./responses/group-response.class');

class Group extends BackendModule {
  constructor(api) {
    super(api);
    this.apiEndPoint = '/group';
  }

  getByID(request, response, groupID) {
    let cls = this;
    this.api.db.get(
      'SELECT * FROM groups WHERE groupID=?',
      [groupID],
      function(err, row) {
        if (err) {
          cls.respond(request, response, err);
        } else {
          let gResData = new gr.GroupResponseData();
          gResData.groupID = row.groupID;
          gResData.groupName = row.groupName;
          gResData.owner = row.owner;
          gResData.members = (row.members) ? row.members.split(',').map((n) => { return parseInt(n); }) : [];
          let responseObject = new gr.GroupResponse();
          responseObject.data.push(gResData);
          cls.respond(request, response, responseObject);
        }
      }
    );
  }

  save(request, response, body) {
    if (this.requireBodyElements(request, response, body, ['userID', 'groupID', 'groupName', 'members'])) {
      let cls = this;
      if (body.groupID > 0) {
        this.db.run(
          'UPDATE groups SET groupName=?, members=? WHERE groupID=?',
          [body.groupName.trim(), body.members.join(','), body.groupID],
          function(err) {
            if (err) {
              cls.respond(request, response, false);
            } else {
              cls.respond(request, response, true);
            }
          }
        );
      } else {
        let groupName = body.groupName.trim();
        let members = body.members.join(',');
        this.api.db.run(
          'INSERT INTO groups (groupName, members) VALUES (?, ?)',
          [groupName, members],
          function(err) {
            if (err) {
              that.respond(request, response, false);
            } else {
              let gResData = new gr.GroupResponseData();
              gResData.groupID = this.lastID;
              gResData.groupName = groupName;
              gResData.members = members.split(',').map((n) => { return parseInt(n); });
              gResData.owner = body.userID;
              let responseObject = new gr.GroupResponse();
              responseObject.data.push(gResData);
              that.respond(request, response, responseObject);
            }
          }
        );
      }
    }
  }

  deleteByID(request, response, body) {
    if (this.requireBodyElements(request, response, body, ['userID', 'groupID'])) {
      let cls = this;
      let groupID = body.groupID;
      this.api.db.run(
        'DELETE FROM groups WHERE groupID=?',
        [groupID],
        function(err) {
          if (err) {
            cls.respond(request, response, err);
          } else {
            cls.api.db.get(
              'SELECT groups FROM users WHERE userID=?',
              [userID],
              function(err, row) {
                if (err) {
                  cls.respond(request, response, err);
                } else {
                  let groups = row.groups.filter(g => g !== groupID);
                  cls.api.db.run(
                    'UPDATE users SET groups=? WHERE userID=?',
                    [groups, userID],
                    function(err) {
                      if (err) {
                        cls.respond(request, response, false);
                      } else {
                        cls.respond(request, response, true);
                      }
                    }
                  );
                }
              }
            );
          }
        }
      );
    }
  }

  internalRoute(request, response, body) {
    let cls = this;
    this._validateAPIKeyAgainstUserID(request, body, valid => {
      if (valid) {
        if (request.method === 'GET') {
          let groupID = parseInt(request.url.split('/').pop());
          if (!isNaN(groupID)) {
            this.getByID(request, response, groupID);
          }
        } else if (request.method === 'POST') {
          this.save(request, response, body);
        } else if (request.method === 'PUT') {
          this.save(request, response, body);
        } else if (request.method === 'DELETE') {
          this.deleteByID(request, response, body);
        }
      } else {
        cls.respond(request, response, Error.ErrorCodes.MALFORMED_REQUEST);
      }
    });

  }
}

module.exports = {
  'Group': Group
};
