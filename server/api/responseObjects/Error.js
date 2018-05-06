let BaseResponse = require('./BaseResponse.js');

const ErrorCodes = {
    // 100-199: API errors
    'UNKNOWN_ERROR': 100,
    'NO_BODY': 101,
    'MALFORMED_REQUEST': 102,
    // 200-299: User errors
    'PASSWORDS_MISMATCH': 200
};

const ErrorMessages = {
    100: 'Unknown error',
    101: 'No body',
    102: 'Malformed request body',
    200: 'Passwords does not match'
};

class ErrorResponse extends BaseResponse {
  constructor() {
    super();
    this.type = 'ErrorResponse';
  }
}

class Error {
    constructor() {
        this.type = 'Error';
    }
}

module.exports = {
    'ErrorResponse': ErrorResponse,
    'Error': Error,
    'ErrorCodes': ErrorCodes,
    'ErrorMessages': ErrorMessages
}
