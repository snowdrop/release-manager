let jsontoyaml = require('json2yaml')
let yaml = require('js-yaml')
let Log = require('./log')
let logger = require('winston')
var log = new Log()


module.exports = {
  addValueToList,
  convertJsontoObject,
  convertToBase64,
  Log: new Log(),
  Logger: new newWinstonLogger()
}

function newWinstonLogger() {
    return logger.createLogger({
        level: process.env.LOG_LEVEL,
        format: logger.format.simple(),
        transports: [
            new logger.transports.Console()
        ]
    });
}
function addValueToList (map, key, value) {
  // if the list is already created for the "key", then uses it
  // else creates new list for the "key" to store multiple values in it.
  map[key] = map[key] || []
  map[key].push(value)
  return map
}

function convertToBase64 (username, password) {
  var userAndPassword = username + ':' + password
  return Buffer.from(userAndPassword).toString('base64')
}

// Convert JSON to YAML and JS Objects
function convertJsontoObject (result) {
  var ymlText = jsontoyaml.stringify(result.fields)
  log.debug(yaml.safeLoad(ymlText))
  var type = yaml.safeLoad(ymlText)
  return type
}
