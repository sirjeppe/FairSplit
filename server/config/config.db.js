const process = require('process');
const fs = require('fs');
const path = require('path');
const sqlite3 = require('sqlite3').verbose();
const config = require('../config.js');

let setupDB = function() {
  console.log('Setting up database...');
  let db = new sqlite3.Database(path.join(process.cwd(), config['dbFolder'], 'db.db'));
  console.log('Creating users table...');
  db.run('CREATE TABLE IF NOT EXISTS users (userID INTEGER PRIMARY KEY AUTOINCREMENT, userName TEXT UNIQUE COLLATE NOCASE NOT NULL, password TEXT NOT NULL, income INTEGER DEFAULT 0, groups TEXT, apiKey TEXT, keyValidTo INTEGER DEFAULT 0)');
  console.log('Creating groups table...');
  db.run('CREATE TABLE IF NOT EXISTS groups (groupID INTEGER PRIMARY KEY AUTOINCREMENT, groupName TEXT NOT NULL, owner INTEGER NOT NULL, members TEXT NOT NULL)');
  console.log('Creating invites table...');
  db.run('CREATE TABLE IF NOT EXISTS invites (inviteID INTEGER PRIMARY KEY AUTOINCREMENT, groupID TEXT NOT NULL, userName TEXT NOT NULL, sender TEXT NOT NULL, status INTEGER DEFAULT 0)');
  console.log('Creating expenses table...');
  db.run('CREATE TABLE IF NOT EXISTS expenses (expenseID INTEGER PRIMARY KEY AUTOINCREMENT, groupID INTEGER NOT NULL, userID INTEGER NOT NULL, amount REAL NOT NULL, title TEXT NOT NULL, comment TEXT, datetime INTEGER NOT NULL)');
  db.close();
  console.log('Setting up database done!');
}

module.exports = {
  'db': setupDB
};
