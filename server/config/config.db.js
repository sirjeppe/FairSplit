const process = require('process');
const fs = require('fs');
const path = require('path');
const sqlite3 = require('sqlite3').verbose();
const config = require('../config.js');

let setupDB = function() {
  console.log('Setting up database...');
  let db = new sqlite3.Database(path.join(process.cwd(), config['dbFolder'], 'db.db'));
  console.log('Creating users table...');
  db.run('CREATE TABLE IF NOT EXISTS users (userID INTEGER PRIMARY KEY AUTOINCREMENT, userName TEXT UNIQUE, password TEXT, income INTEGER DEFAULT 0, groups TEXT, apiKey TEXT, keyValidTo INTEGER DEFAULT 0)');
  console.log('Creating groups table...');
  db.run('CREATE TABLE IF NOT EXISTS groups (groupID INTEGER PRIMARY KEY AUTOINCREMENT, groupName TEXT, members TEXT)');
  console.log('Creating expenses table...');
  db.run('CREATE TABLE IF NOT EXISTS expenses (expenseID INTEGER PRIMARY KEY AUTOINCREMENT, groupID INTEGER NOT NULL, userID INTEGER NOT NULL, amount REAL NOT NULL, title TEXT, comment TEXT, datetime INTEGER NOT NULL)');
  db.close();
  console.log('Setting up database done!');
}

module.exports = {
  'db': setupDB
};
