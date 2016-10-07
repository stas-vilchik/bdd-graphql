import React from 'react';
import ReactDOM from 'react-dom';
import GraphiQL from 'graphiql';
import fetch from 'isomorphic-fetch';

import 'graphiql/graphiql.css';
import './index.css';

function graphQLFetcher(graphQLParams) {
  return fetch('http://localhost:8080/graphql', {
    method: 'post',
    headers: { 'Accept': 'application/json' },
    body: JSON.stringify(graphQLParams),
  }).then(response => response.json());
}

ReactDOM.render(<GraphiQL fetcher={graphQLFetcher} />, document.body);
