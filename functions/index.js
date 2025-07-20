/**
 * Import function triggers from their respective submodules:
 *
 * See a full list of supported triggers at https://firebase.google.com/docs/functions
 */

const functions = require("firebase-functions");
const admin = require("firebase-admin");
const nodemailer = require("nodemailer");

admin.initializeApp();

// Configura tu correo y app password de Gmail
const transporter = nodemailer.createTransport({
  service: "gmail",
  auth: {
    user: "stayflow.app@gmail.com",
    pass: "", // App Password
  },
});

exports.sendWelcomeEmail = functions.auth.user().onCreate((user) => {
  const email = user.email;
  const displayName = user.displayName || "usuario";

  if (!email) {
    console.error("No email found for user");
    return Promise.resolve();
  }

  const mailOptions = {
    from: "StayFlow <stayflow.app@gmail.com>",
    to: email,
    subject: "¡Bienvenido a STAYFLOW! 🎉",
    text:
    `Hola ${displayName},\n\nGracias por descargar STAYFLOW.\n` +
    "¡Termina tu registro para poder reservar los mejores hoteles del país!",
  };

  return transporter.sendMail(mailOptions)
      .then(() => console.log("Correo enviado a", email))
      .catch((error) => console.error("Error al enviar correo:", error));
});
