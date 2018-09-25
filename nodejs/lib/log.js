'use strict'

// const colors = require('colors');

const RED = '\x1b[31m'
const GREEN = '\x1b[32m'
const YELLOW = '\x1b[33m'
const BLUE = '\x1b[34m'
const MAGENTA = '\x1b[35m'
const CYAN = '\x1b[36m'
const UNDERLINE = '\x1B[4m'
const UNDERLINE_RESET = '\x1B[24m'
const RESET = '\x1b[39m'

class Log {
  static cyan (str) {
    return `${CYAN}${str}${RESET}`
  }

  static blue (str) {
    return `${BLUE}${str}${RESET}`
  }

  static magenta (str) {
    return `${MAGENTA}${str}${RESET}`
  }

  static yellow (str) {
    return `${YELLOW}${str}${RESET}`
  }

  static red (str) {
    return `${RED}${str}${RESET}`
  }

  static green (str) {
    return `${GREEN}${str}${RESET}`
  }

  static underline (str) {
    return `${UNDERLINE}${str}${UNDERLINE_RESET}`
  }

  transform (action, source, destination) {
    console.log(`${action}' '${Log.cyan(source)}' to '${Log.cyan(destination)}`)
  }

  debug (message) {
    console.log(` - ${Log.blue(message)}`)
  }

  info (message) {
    console.log(Log.magenta(message))
  }

  warn (message) {
    console.log(Log.yellow(message))
  }

  error (message) {
    console.log(Log.red(message))
  }

  success (message) {
    console.log('')
    console.log(Log.green('>>') + ' ' + message)
  }

  title (message) {
    console.log('')
    console.log(Log.underline(message))
  }
}

module.exports = Log
