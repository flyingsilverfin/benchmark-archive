const config = {};

config.es = {};
config.web = {};

config.es.host = 'localhost';
config.es.port = 9200;
config.web.port = process.env.WEB_PORT || 4567;

module.exports = config;