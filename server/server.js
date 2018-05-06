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
  let files = fs.readdirSync(config['configFolder']);
  for (let i = 0; i < files.length; i++) {
    let configFile = [config['configFolder'], files[i]].join('/');
    let temp = require(configFile);
    for (let m in temp) {
      temp[m].call();
    }
  }
}

let db = new sqlite3.Database(path.join(process.cwd(), config['dbFolder'], 'db.db'));

process.on('SIGINT', function() {
  console.log('Shutting down...');
  db.close();
  process.exit();
});

let server = function(request, response) {
  let uri = url.parse(request.url).pathname;

  if (uri.startsWith('/api/')) {

    // Do API parsing
    delete require.cache[require.resolve('./api/api.js')];
    try {
      let api = require('./api/api.js');
      api.handle(db, request, response);
    } catch (err) {
      console.error(err);
    }

  } else {

    let filename = path.join(process.cwd(), 'html', uri);

    fs.exists(filename, function(exists) {
      if (!exists) {
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
