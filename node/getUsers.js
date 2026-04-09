module.exports.handler = async () => {
  return {
    statusCode: 200,
    body: JSON.stringify([
      { id: 1, nombre: "Sebastian", email: "test@test.com" },
      { id: 2, nombre: "Ana", email: "ana@test.com" }
    ])
  };
};