const graphqlHTTP = require('express-graphql');
const { makeExecutableSchema } = require('graphql-tools');

function SpansController(esClient){
    this.client = esClient;
}


const typeDefs = `
  type Query {
    querySpans(graphName: String,
        executionName: String,
        orderBy: String, 
        order: String, 
        offset: Int, 
        limit: Int): [Span]
  }

  type Span {
    duration: Int!
    name: String!
    tags: Tag
  }

  type Tag {
    executionName: String,
    graphName: String,
    query: String,
    repetition: Int,
    repetitions: Int,
    scale: Int
  }
`;

function filterQuerySpans(args){
    const must = [{ match: { name: "query"} } ];
    if(args.graphName) {
        must.push({ match: { "tags.graphName": args.graphName }});
    }
    if(args.executionName) {
        must.push({ match: { "tags.executionName": args.executionName }});
    }
    return { query: { bool: { must } } };
}

function limitQuery(offset, limit){
    return {from : offset || 0 , size : limit || 50};
}

const resolvers = {
    Query: {
        querySpans: (object, args, context, info) => {
            let body = {};
            Object.assign(body, limitQuery(args.offset, args.limit));
            Object.assign(body, filterQuerySpans(args));
            const queryObject = Object.assign({index: "benchmark*", type: "span"}, { body });
            return context.client.search(queryObject).then(result => result.hits.hits.map(res => res._source));
        }
    }
}

const schema = makeExecutableSchema({typeDefs, resolvers})

SpansController.prototype.query = function query(){
    return graphqlHTTP({
        schema,
        context: { client: this.client},
    });
}


module.exports = SpansController;