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

// Nueva función: correo personalizado cuando se completa el registro
exports.enviarCorreoBienvenida = functions.firestore
    .document("usuarios/{userId}")
    .onCreate(async (snap, context) => {
      const userData = snap.data();
      const userId = context.params.userId;

      try {
        const userRecord = await admin.auth().getUser(userId);
        const email = userRecord.email;

        let mailOptions;

        if (userData.rol === "driver") {
          mailOptions = {
            from: "StayFlow <stayflow.app@gmail.com>",
            to: email,
            subject: "¡Gracias por registrarte como conductor en StayFlow!",
            html: `
              <div style="font-family: Arial, sans-serif;
                          max-width: 600px; margin: 0 auto;">
                <div style="background-color: #3C3C7C;
                            padding: 20px; text-align: center;">
                  <h1 style="color: white; margin: 0;">
                    ¡Bienvenido a StayFlow!
                  </h1>
                </div>
                <div style="padding: 30px; background-color: #f9f9f9;">
                  <h2 style="color: #3C3C7C;">
                    Hola ${userData.nombres} ${userData.apellidos},
                  </h2>
                  <p>¡Gracias por registrarte como conductor en StayFlow!</p>
                  <p>Tu solicitud ha sido recibida exitosamente.
                     Nuestro equipo revisará tu información y activará
                     tu cuenta en las próximas 24-48 horas.</p>
                  <div style="background-color: #e8f4fd; padding: 20px;
                              border-radius: 8px; margin: 20px 0;">
                    <h3 style="color: #3C3C7C; margin-top: 0;">
                      Información de tu registro:
                    </h3>
                    <ul>
                      <li><strong>Nombre:</strong>
                          ${userData.nombres} ${userData.apellidos}</li>
                      <li><strong>Documento:</strong>
                          ${userData.tipoDocumento} -
                          ${userData.numeroDocumento}</li>
                      <li><strong>Teléfono:</strong> ${userData.telefono}</li>
                      <li><strong>Vehículo:</strong>
                          ${userData.modelo} - Placa ${userData.placa}</li>
                    </ul>
                  </div>
                  <p><strong>¿Qué sigue?</strong></p>
                  <ul>
                    <li>Revisaremos tu documentación</li>
                    <li>Verificaremos la información del vehículo</li>
                    <li>Te notificaremos cuando tu cuenta esté activa</li>
                  </ul>
                  <p>Una vez activada tu cuenta, podrás comenzar a recibir
                     solicitudes de viajes desde el aeropuerto hasta hoteles.
                  </p>
                  <p>¡Esperamos tenerte pronto en nuestro equipo!</p>
                  <p style="color: #666;">
                    Saludos,<br>
                    El equipo de StayFlow
                  </p>
                </div>
              </div>
            `,
          };
        } else {
          mailOptions = {
            from: "StayFlow <stayflow.app@gmail.com>",
            to: email,
            subject: "¡Bienvenido a StayFlow - Tu compañero de viaje!",
            html: `
              <div style="font-family: Arial, sans-serif;
                          max-width: 600px; margin: 0 auto;">
                <div style="background-color: #3C3C7C;
                            padding: 20px; text-align: center;">
                  <h1 style="color: white; margin: 0;">
                    ¡Bienvenido a StayFlow!
                  </h1>
                </div>
                <div style="padding: 30px; background-color: #f9f9f9;">
                  <h2 style="color: #3C3C7C;">Hola ${userData.nombres},</h2>
                  <p>¡Tu registro se ha completado exitosamente!</p>
                  <div style="background-color: #e8f4fd; padding: 20px;
                              border-radius: 8px; margin: 20px 0;">
                    <h3 style="color: #3C3C7C; margin-top: 0;">
                      ¿Qué puedes hacer en StayFlow?
                    </h3>
                    <div style="margin: 15px 0;">
                      <h4 style="color: #3C3C7C; margin: 5px 0;">
                        🏨 Reservar Hoteles
                      </h4>
                      <p style="margin: 5px 0;">
                        Encuentra y reserva los mejores hoteles
                      </p>
                    </div>
                    <div style="margin: 15px 0;">
                      <h4 style="color: #3C3C7C; margin: 5px 0;">
                        🚕 Solicitar Taxis
                      </h4>
                      <p style="margin: 5px 0;">
                        Pide un taxi desde el aeropuerto hasta tu hotel
                      </p>
                    </div>
                    <div style="margin: 15px 0;">
                      <h4 style="color: #3C3C7C; margin: 5px 0;">
                        ⭐ Escribir Reseñas
                      </h4>
                      <p style="margin: 5px 0;">
                        Comparte tu experiencia con otros viajeros
                      </p>
                    </div>
                  </div>
                  <p>¡Tu cuenta está lista para usar!</p>
                  <p style="color: #666;">
                    ¡Que tengas excelentes viajes!<br>
                    El equipo de StayFlow
                  </p>
                </div>
              </div>
            `,
          };
        }

        await transporter.sendMail(mailOptions);
        console.log("Correo de bienvenida personalizado enviado a:", email);
      } catch (error) {
        console.error("Error al enviar correo de bienvenida:", error);
      }
    });
