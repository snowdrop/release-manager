let jsontoyaml = require('json2yaml')
let yaml = require('js-yaml')

module.exports = {
  addValueToList,
  convertJsontoObject,
  convertToBase64
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
function convertJsontoObject(result) {
    ymlText = jsontoyaml.stringify(result.fields)
    //console.log(yaml.safeLoad(ymlText))
    type = yaml.safeLoad(ymlText)
    return type
}
