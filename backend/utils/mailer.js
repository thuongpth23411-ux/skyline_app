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
        from: `"SKYLINE" <${process.env.EMAIL_USER}>`,
        to: email,
        subject: "Mã xác thực OTP - SKYLINE",
        html: `
            <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px; border: 1px solid #e0e0e0; border-radius: 10px;">
                <h2 style="color: #0B2C86; text-align: center;">Xác thực tài khoản SKYLINE</h2>
                <p>Chào bạn,</p>
                <p>Cảm ơn bạn đã lựa chọn SKYLINE. Đây là mã OTP của bạn để hoàn tất quá trình xác thực:</p>
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
        return true;
    } catch (error) {
        console.error("❌ Error sending email:", error);
        return false;
    }
};

const sendTicketEmail = async (email, ticketInfo) => {
    const { bookingCode, passengerName, tickets, tempAccount, isExchange, isCancel } = ticketInfo;

    const qrCodeUrl = `https://api.qrserver.com/v1/create-qr-code/?size=150x150&data=${bookingCode}`;

    let subjectPrefix = "Đặt vé thành công";
    let statusLabel = "THANH TOÁN THÀNH CÔNG";
    let mainHeading = "Hành trình của bạn đã sẵn sàng!";
    let headerColor = "#0B4DA2";
    let statusColor = "#22C55E";

    if (isExchange) {
        subjectPrefix = "Thay đổi hành trình thành công";
        statusLabel = "ĐỔI VÉ THÀNH CÔNG";
        mainHeading = "Vé mới của bạn đã được cập nhật!";
        headerColor = "#0284c7"; // Blue-600
        statusColor = "#0284c7";
    } else if (isCancel) {
        subjectPrefix = "Hủy vé thành công";
        statusLabel = "XÁC NHẬN HỦY VÉ";
        mainHeading = "Yêu cầu hủy vé đã được xử lý!";
        headerColor = "#475569"; // Gray-600
        statusColor = "#dc2626"; // Red-600
    }

    let ticketsHtml = tickets.map(t => {
        const details = t.flightDetails || {};
        return `
            <table width="100%" style="margin-bottom: 15px; background: #ffffff; border: 1px solid #e2e8f0; border-radius: 8px; border-collapse: separate; border-spacing: 0;">
                <tr>
                    <td style="padding: 10px 15px; background: #F9F9F9; border-bottom: 1px dashed #e2e8f0; border-top-left-radius: 8px; border-top-right-radius: 8px;">
                        <span style="font-size: 10px; color: ${headerColor}; font-weight: bold; text-transform: uppercase;">${t.ticketType}</span>
                    </td>
                    <td style="padding: 10px 15px; background: #F9F9F9; border-bottom: 1px dashed #e2e8f0; text-align: right;">
                        <span style="font-size: 10px; color: #64748b;">Số hiệu: <strong>${details.flightNumber || "---"}</strong></span>
                    </td>
                </tr>
                <tr>
                    <td colspan="2" style="padding: 15px;">
                        <table width="100%">
                            <tr>
                                <td width="42%" style="font-size: 14px; font-weight: bold; color: #143E7A; line-height: 1.2;">
                                    <div style="font-size: 18px; color: ${headerColor};">${details.departureCode || "---"}</div>
                                    <div style="font-size: 10px; font-weight: normal; color: #496A98; margin-top: 2px;">${details.departureAirport || "---"}</div>
                                </td>
                                <td width="16%" style="text-align: center; color: ${headerColor}; font-size: 18px;">✈</td>
                                <td width="42%" style="font-size: 14px; font-weight: bold; color: #143E7A; text-align: right; line-height: 1.2;">
                                    <div style="font-size: 18px; color: ${headerColor};">${details.arrivalCode || "---"}</div>
                                    <div style="font-size: 10px; font-weight: normal; color: #496A98; margin-top: 2px;">${details.arrivalAirport || "---"}</div>
                                </td>
                            </tr>
                        </table>
                        <div style="margin-top: 12px; padding-top: 10px; border-top: 1px solid #f1f5f9; font-size: 11px; color: #496A98;">
                            Ghế: <strong style="color: ${headerColor};">${t.seatId}</strong> | Mã vé: <strong>${t.ticketId}</strong>
                        </div>
                    </td>
                </tr>
            </table>
        `;
    }).join('');

    let accountHtml = "";
    if (tempAccount) {
        accountHtml = `
            <div style="margin: 25px 0; background-color: #FFF9EE; border: 1px solid #FFEDCC; border-radius: 12px; padding: 15px;">
                <h4 style="margin: 0 0 10px 0; color: #E65100; font-size: 11px; white-space: nowrap; letter-spacing: 0.5px;">🎁 THÔNG TIN TÀI KHOẢN HỘI VIÊN</h4>
                <p style="margin: 0; font-size: 10px; color: #b45309; line-height: 1.4; opacity: 0.9;">Tài khoản đã được tạo tự động để bạn dễ dàng quản lý hành trình:</p>
                <table width="100%" style="margin-top: 12px; background: #ffffff; border-radius: 8px; border: 1px solid #FFEDCC; border-collapse: collapse;">
                    <tr>
                        <td style="padding: 10px; font-size: 11px; color: #64748b; white-space: nowrap; border-bottom: 1px solid #fef3c7;">Tên đăng nhập:</td>
                        <td style="padding: 10px; font-size: 11px; font-weight: bold; color: #0B4DA2; text-align: right; border-bottom: 1px solid #fef3c7; word-break: break-all;">${tempAccount.email}</td>
                    </tr>
                    <tr>
                        <td style="padding: 10px; font-size: 11px; color: #64748b; white-space: nowrap;">Mật khẩu tạm thời:</td>
                        <td style="padding: 10px; text-align: right; white-space: nowrap;">
                            <span style="background-color: #FFEB3B; color: #000; font-weight: bold; padding: 3px 8px; border-radius: 4px; font-size: 12px; font-family: 'Courier New', Courier, monospace;">${tempAccount.password}</span>
                        </td>
                    </tr>
                </table>
                <p style="margin-top: 10px; font-size: 10px; color: #d97706; font-style: italic;">* Vui lòng đăng nhập vào ứng dụng để xem chi tiết vé của bạn.</p>
            </div>
        `;
    }

    const mailOptions = {
        from: `"SKYLINE" <${process.env.EMAIL_USER}>`,
        to: email,
        subject: `[SKYLINE] ${subjectPrefix} - PNR: ${bookingCode}`,
        html: `
            <div style="font-family: 'Segoe UI', Roboto, Helvetica, Arial, sans-serif; max-width: 550px; margin: 0 auto; background-color: #ffffff; border: 1px solid #e2e8f0; border-radius: 16px; overflow: hidden; box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1);">
                <!-- Header -->
                <div style="background-color: ${headerColor}; padding: 25px; text-align: center;">
                    <h1 style="margin: 0; color: #ffffff; font-size: 22px; letter-spacing: 6px; font-weight: 800;">SKYLINE</h1>
                    <p style="margin: 5px 0 0 0; font-size: 9px; letter-spacing: 3px; color: #DDEEFF; text-transform: uppercase;">Trải nghiệm bay đẳng cấp</p>
                </div>

                <div style="padding: 30px 25px;">
                    <!-- Success Header -->
                    <div style="text-align: center; margin-bottom: 25px;">
                        <p style="margin: 0; color: ${statusColor}; font-weight: bold; font-size: 12px; letter-spacing: 0.5px;">${statusLabel}</p>
                        <h2 style="margin: 5px 0; font-size: 16px; color: #143E7A;">${mainHeading}</h2>
                        <p style="margin: 10px 0 0 0; font-size: 11px; color: #496A98;">Chào <strong>${passengerName}</strong>, ${isCancel ? "yêu cầu hoàn vé của bạn đã được xác nhận." : "cảm ơn bạn đã tin tưởng dịch vụ của chúng tôi."}</p>
                    </div>

                    ${isCancel ? "" : `
                    <!-- PNR & QR Section -->
                    <table width="100%" style="background-color: #F9F9F9; border: 1px solid #EDF4FF; border-radius: 12px; padding: 20px; margin-bottom: 25px;">
                        <tr>
                            <td align="center">
                                <p style="margin: 0; font-size: 9px; color: #496A98; text-transform: uppercase; letter-spacing: 1px; font-weight: bold;">Mã đặt chỗ (PNR)</p>
                                <p style="margin: 4px 0 15px 0; font-size: 24px; color: ${headerColor}; font-weight: bold; letter-spacing: 3px;">${bookingCode}</p>
                                <div style="background: #ffffff; display: inline-block; padding: 10px; border-radius: 12px; border: 1px solid #DDEEFF;">
                                    <img src="${qrCodeUrl}" width="130" height="130" style="display: block;" alt="QR" />
                                </div>
                                <p style="margin: 15px 0 0 0; font-size: 9px; color: #496A98; font-weight: 500;">(Quét mã này tại quầy để làm thủ tục nhanh hơn)</p>
                            </td>
                        </tr>
                    </table>
                    `}

                    <!-- Ticket List -->
                    <div style="margin-bottom: 10px;">
                        <p style="margin: 0 0 12px 0; font-size: 11px; font-weight: bold; color: #143E7A; border-bottom: 1px solid #F0F0F0; padding-bottom: 5px; text-transform: uppercase;">${isCancel ? "Thông tin vé đã hủy" : "Chi tiết chuyến bay"}</p>
                        ${ticketsHtml}
                    </div>

                    ${accountHtml}

                    ${isCancel ? `
                    <div style="background-color: #FEF2F2; border-radius: 8px; padding: 15px; margin-top: 20px; border: 1px solid #FEE2E2;">
                        <p style="margin: 0 0 5px 0; font-size: 11px; color: #991B1B; font-weight: bold;">Thông tin hoàn tiền:</p>
                        <p style="margin: 0; font-size: 10px; color: #B91C1C; line-height: 1.6;">
                            Số tiền hoàn lại sẽ được chuyển vào tài khoản thanh toán ban đầu của quý khách trong vòng 3-5 ngày làm việc tùy theo ngân hàng.
                        </p>
                    </div>
                    ` : `
                    <!-- Notice -->
                    <div style="background-color: #F9F9F9; border-radius: 8px; padding: 15px; margin-top: 20px;">
                        <p style="margin: 0 0 5px 0; font-size: 11px; color: #143E7A; font-weight: bold;">Lưu ý quan trọng:</p>
                        <ul style="margin: 0; padding-left: 18px; font-size: 10px; color: #496A98; line-height: 1.6;">
                            <li>Mang theo CCCD/Hộ chiếu bản gốc.</li>
                            <li>Có mặt tại sân bay trước 90-120 phút.</li>
                            <li>Làm thủ tục trực tuyến trước 24h để chọn chỗ ngồi ưng ý.</li>
                        </ul>
                    </div>
                    `}

                    <!-- Thank you -->
                    <div style="margin-top: 40px; padding-top: 30px; border-top: 1px solid #F0F0F0; text-align: center;">
                        <p style="margin: 0; font-style: italic; color: #496A98; font-size: 13px; line-height: 1.6;">
                            "Một lần nữa, SKYLINE xin chân thành cảm ơn Quý khách. Chúc Quý khách có một hành trình an toàn và đầy thú vị!"
                        </p>
                        <p style="margin: 15px 0 0 0; font-weight: bold; color: ${headerColor}; font-size: 13px;">Đội ngũ SKYLINE</p>
                    </div>
                </div>

                <!-- Footer -->
                <div style="background-color: #F9F9F9; padding: 20px; text-align: center; font-size: 10px; color: #8390AD; border-top: 1px solid #F0F0F0;">
                    Hotline: 1900 1234 | support@skyline.vn<br>
                    © 2025 SKYLINE. Mọi quyền được bảo lưu.
                </div>
            </div>
        `
    };

    try {
        await transporter.sendMail(mailOptions);
        return true;
    } catch (error) {
        console.error("❌ Error sending ticket email:", error);
        return false;
    }
};

module.exports = { sendOTP, sendTicketEmail };
