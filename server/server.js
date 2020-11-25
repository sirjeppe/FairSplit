const http = require('http');
const https = require('https');
const url = require('url');
const path = require('path');
const fs = require('fs');
const process = require('process');
const config = require('./config.js');
const sqlite3 = require('sqlite3').verbose();
const port = 54321;

// If db folder doesn't exist - run server config/setup
if (!fs.existsSync(config['dbFolder'])) {
  fs.mkdirSync(config['dbFolder']);
  const files = fs.readdirSync(config['configFolder']);
  for (let i = 0; i < files.length; i++) {
    const configFile = [config['configFolder'], files[i]].join('/');
    const temp = require(configFile);
    for (let m in temp) {
      temp[m].call();
    }
  }
}

const db = new sqlite3.Database(path.join(process.cwd(), config['dbFolder'], 'db.db'));
const API = require('./api/api.js');
const api = new API(db);

process.on('SIGINT', function() {
  console.log('Shutting down...');
  db.close();
  process.exit();
});

const server = function(request, response) {
  let uri = url.parse(request.url).pathname;

  if (uri.startsWith('/api/')) {

    // Do API parsing
    try {
      api.route(request, response);
      // api.handle(db, request, response);
    } catch (err) {
      console.error(err);
    }

  } else {

    let filename = path.join(process.cwd(), 'html', uri);

    fs.stat(filename, function(err, stats) {
      if (!stats) {
        response.writeHead(404, {'Content-Type': 'text/plain'});
        response.write('404 Not Found\n');
        response.end();
        return;
      }

      if (fs.statSync(filename).isDirectory()) filename += '/index.html';

      fs.readFile(filename, 'binary', function(err, file) {
        if (err) {
          response.writeHead(500, {'Content-Type': 'text/plain'});
          response.write(err + '\n');
          response.end();
          return;
        }

        response.writeHead(200);
        response.write(file, 'binary');
        response.end();
      });
    });

  }
}

if (fs.existsSync(config['certsFolder'])) {
  var options = {
    key: fs.readFileSync(path.join(process.cwd(), config['certsFolder'], '/privkey.pem')),
    cert: fs.readFileSync(path.join(process.cwd(), config['certsFolder'], '/cert.pem')),
    ca: fs.readFileSync(path.join(process.cwd(), config['certsFolder'], '/chain.pem'))
  };
  https.createServer(options, server).listen(port);
  console.log('Secure server started on port ' + port);
} else {
  http.createServer(server).listen(port);
  console.log('Insecure server started on port ' + port);
}
