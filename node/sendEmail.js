const { SNSClient, PublishCommand } = require("@aws-sdk/client-sns");

const snsClient = new SNSClient({ region: "us-east-1" });

const TOPIC_ARN = process.env.TOPIC_ARN;

function parseMessage(body) {
    return JSON.parse(body || "{}");
}

function validateUser(user) {
    if (!user.id || !user.nombre || !user.email) {
        throw new Error("Mensaje inválido: faltan datos del usuario");
    }
}

function buildEmailMessage(user) {
  return {
    Subject: "Nuevo usuario creado",
    Message: `
    Se ha creado un nuevo usuario:

    ID: ${user.id}
    Nombre: ${user.nombre}
    Email: ${user.email}
        `,
    };
}

async function sendEmail({ Subject, Message }) {
    const command = new PublishCommand({
        TopicArn: TOPIC_ARN,
        Subject,
        Message,
    });

    await snsClient.send(command);
}

module.exports.handler = async (event) => {
    try {
        if (!event.Records || event.Records.length === 0) {
        console.warn("No hay mensajes en el evento");
        return;
        }

        for (const record of event.Records) {
        const user = parseMessage(record.body);

        validateUser(user);

        const message = buildEmailMessage(user);

        await sendEmail(message);
        }

    } catch (error) {
        console.error("Error procesando mensajes:", error);
        throw error;
    }
};