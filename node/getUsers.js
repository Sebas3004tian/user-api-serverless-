const { DynamoDBClient, ScanCommand } = require("@aws-sdk/client-dynamodb");

const client = new DynamoDBClient({
  region: "us-east-1",
});

const TABLE_NAME = process.env.TABLE_NAME;

module.exports.handler = async () => {
  try {
    const command = new ScanCommand({
      TableName: TABLE_NAME,
    });

    const response = await client.send(command);

    const users = (response.Items || []).map(item => ({
      id: item.id?.S,
      nombre: item.nombre?.S,
      email: item.email?.S,
    }));

    return {
      statusCode: 200,
      body: JSON.stringify(users),
    };

  } catch (error) {
    return {
      statusCode: 500,
      body: JSON.stringify({ error: error.message }),
    };
  }
};