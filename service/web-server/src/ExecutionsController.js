const graphqlHTTP = require('express-graphql');
const { makeExecutableSchema } = require('graphql-tools');

const GRAKN_BENCHMARK_INDEX = 'grakn-benchmark';
const BENCHMARK_EXECUTION_TYPE = 'execution';
const SEARCH_EXECUTIONS_OBJECT = { index: GRAKN_BENCHMARK_INDEX, type: BENCHMARK_EXECUTION_TYPE};

function ExecutionsController(client){
    this.client = client
}

ExecutionsController.prototype.addExecution = function add(execution) {
    return this.client.create({
        index: GRAKN_BENCHMARK_INDEX,
        type: BENCHMARK_EXECUTION_TYPE,
        id: execution.id,
        body: {
          commit: execution.commit,
          prMergedAt: execution.prMergedAt,
          prUrl: execution.prUrl,
          prNumber: execution.prNumber,
          repoId: execution.repoId,
          repoUrl: execution.repoUrl,
          executionStartedAt: execution.executionStartedAt,
          executionCompletedAt: execution.executionCompletedAt,
          status: execution.status,
          vmName: execution.vmName,
        }
      });
}

ExecutionsController.prototype.executionRunning = function started(execution) {
    return this.client.update({
        index: GRAKN_BENCHMARK_INDEX,
        type: BENCHMARK_EXECUTION_TYPE,
        id: execution.executionId,
        body: {
          doc: {
            status: 'RUNNING',
            executionStartedAt: new Date().toISOString()
          }
        }
      });
}

ExecutionsController.prototype.executionCompleted = function completed(execution) {
    return this.client.update({
        index: GRAKN_BENCHMARK_INDEX,
        type: BENCHMARK_EXECUTION_TYPE,
        id: execution.executionId,
        body: {
          doc: {
            status: 'COMPLETED',
            executionCompletedAt: new Date().toISOString()
          }
        }
      });
}

ExecutionsController.prototype.executionFailed = function failed(execution) {
    return this.client.update({
        index: GRAKN_BENCHMARK_INDEX,
        type: BENCHMARK_EXECUTION_TYPE,
        id: execution.executionId,
        body: {
          doc: {
            status: 'FAILED',
            executionCompletedAt: new Date().toISOString()
          }
        }
      });
}

// Construct a schema, using GraphQL schema language
const typeDefs = `
  type Query {
    executions(
      status: [String], 
      orderBy: String, 
      order: String,
      offset: Int, 
      limit: Int): [Execution]
  }

  type Execution {
    id: String!,
    commit: String!
    repoUrl: String!
    repoId: String!
    prMergedAt: String!,
    prUrl: String!,
    prNumber: String!,
    executionStartedAt: String
    executionCompletedAt: String
    status: String!
    vmName: String
  }
`;


function queryFilteredByStatus(statusArray){
  const should = statusArray.map(status => ({ match: { status}}));
  return { query: { bool: { should } } } ;
}

function sortResults(orderBy, order){
  const sortingOrder = (order) ? order : "desc";
  return { sort: [ {[orderBy]: sortingOrder} ]};
}

function limitQuery(offset, limit){
  return {from : offset || 0 , size : limit || 50};
}

// The root provides a resolver function for each API endpoint
const root = {
  executions: (object, args, context, info) => {
    let body = {};
    // If a status is provided, only retrieve executions with given status/es
    if(args.status) {
      Object.assign(body, queryFilteredByStatus(args.status));
    } 
    // If ordeBy is provided, order results by field
    if(args.orderBy){
      Object.assign(body, sortResults(args.orderBy, args.order));
    }
    Object.assign(body, limitQuery(args.offset, args.limit));
    const queryObject = Object.assign({}, SEARCH_EXECUTIONS_OBJECT, { body });
    return context.client.search(queryObject).then(result => result.hits.hits.map(res => Object.assign(res._source, {id: res._id})));
  },
};

const schema = makeExecutableSchema({ typeDefs , resolvers:{ Query: root}});


ExecutionsController.prototype.queryExecutions = function query() {
  return graphqlHTTP({
    schema,
    context: { client: this.client},
  });
}

module.exports = ExecutionsController;