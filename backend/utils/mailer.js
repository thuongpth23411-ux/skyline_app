const nodemailer = require("nodemailer");

const transporter = nodemailer.createTransport({
    service: "gmail",
    auth: {
        user: process.env.EMAIL_USER,
        pass: process.env.EMAIL_PASS,
    },
});

const sendOTP = async (email, otp) => {
    const mailOptions = {
        from: `"Skyline App" <${process.env.EMAIL_USER}>`,
        to: email,
        subject: "Mã xác thực OTP - Skyline App",
        html: `
            <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px; border: 1px solid #e0e0e0; border-radius: 10px;">
                <h2 style="color: #0B2C86; text-align: center;">Xác thực tài khoản Skyline</h2>
                <p>Chào bạn,</p>
                <p>Cảm ơn bạn đã lựa chọn Skyline. Đây là mã OTP của bạn để hoàn tất quá trình xác thực:</p>
                <div style="background-color: #f4f7ff; padding: 20px; text-align: center; font-size: 32px; font-weight: bold; letter-spacing: 5px; color: #1E63F2; border-radius: 5px; margin: 20px 0;">
                    ${otp}
                </div>
                <p>Mã này có hiệu lực trong vòng <b>10 phút</b>. Vui lòng không chia sẻ mã này với bất kỳ ai.</p>
                <hr style="border: 0; border-top: 1px solid #e0e0e0; margin: 20px 0;">
                <p style="font-size: 12px; color: #8390AD; text-align: center;">Đây là email tự động, vui lòng không trả lời email này.</p>
            </div>
        `,
    };

    try {
        await transporter.sendMail(mailOptions);
        console.log(`✅ OTP sent to ${email}`);
        return true;
    } catch (error) {
        console.error("❌ Error sending email:", error);
        return false;
    }
};

module.exports = { sendOTP };
