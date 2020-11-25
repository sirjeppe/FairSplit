class BaseResponse {
  constructor() {
    this.requestUri = null;
    this.type = 'BaseResponse';
    this.data = [];
    this.message = null;
    this.errorCode = 0;
  }
}

module.exports = BaseResponse;
