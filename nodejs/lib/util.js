let jsontoyaml = require('json2yaml')
let yaml = require('js-yaml')
let fs = require('fs')
let data = require('../package.json')
let Log = require('./log')
var log = new Log()
const os = require('os')

module.exports = {
  data,
  addValueToList,
  convertJsontoObject,
  convertToBase64,
  parseJIRAConfig,
  Log: new Log()
}

function parseJIRAConfig () {
  try {
    var cfgFile = os.homedir() + '/.jiracli.yml'
    // console.log('Config file : ' + cfgFile)
    return yaml.safeLoad(fs.readFileSync(cfgFile, 'utf8'))
  } catch (e) {
    console.log(e)
  }
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
  // log.debug(yaml.safeLoad(ymlText))
  var type = yaml.safeLoad(ymlText)
  return type
}
