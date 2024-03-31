from flask import Flask, request, jsonify
import smtplib
from email.mime.text import MIMEText
import random
import hashlib
import hmac
import secrets
from service.UserService import UserService
from model.User import User

app = Flask(__name__)

SECRET_KEY = b'c2xhYmV0aGVyaW5nY2FyZQ=='

EMAIL_SENDER = 'vkq0919309031@gmail.com'
PASSWORD_SENDER = 'sylb kytt itvg issz' 

def generate_otp():
    return ''.join(random.choices('0123456789', k=6))

def send_otp_email(email, otp):
    sender_email = EMAIL_SENDER
    password = PASSWORD_SENDER
    message = MIMEText(f"Your OTP confirmation code is: {otp}")
    message['From'] = sender_email
    message['To'] = email
    message['Subject'] = 'OTP Verification'
    with smtplib.SMTP_SSL('smtp.gmail.com', 465) as server:
        server.login(sender_email, password)
        server.sendmail(sender_email, email, message.as_string())
        server.quit()

def generate_token(email, otp, otp_type):
    message = email.encode('utf-8') +('///').encode('utf-8') + otp.encode('utf-8')+('///').encode('utf-8') + otp_type.encode('utf-8')
    token = hmac.new(SECRET_KEY, message, hashlib.sha256).hexdigest()
    return token

@app.route('/auth/create-otp', methods=['POST'])
def create_otp():
    data = request.get_json()
    email_otp = data.get('email_otp')
    otp_type = data.get('otp_type')
    print (email_otp)
    if UserService().get_user_by_email(email_otp) is None and otp_type == 'reset_password':
        return jsonify({'status': 'failed', 'message': 'Email is not found'}), 400
    otp = generate_otp()
    print('created otp for ' + email_otp+ ' with otp type: ' + otp_type)
    print('send otp: ' + otp)
    
    token = generate_token(email_otp, otp, otp_type)

    return jsonify({'status': 'success', 'token': token}), 200

@app.route('/auth/verify-otp', methods=['POST'])
def verify_otp():
    us = UserService()
    data = request.get_json()
    token = data.get('token')
    otp = data.get('otp')
    email = data.get('email')
    otp_type = data.get('otp_type')
    regist_token = generate_token(email,otp,'regist')
    forget_password_token = generate_token(email,otp,'reset_password')
    if token == regist_token:
        print('User is trying to register')
        user = User(email, data.get('username'), data.get('password'), False)
        if us.create_user(user):
            return jsonify({'status': 'success'}), 200
    if token == forget_password_token:
        print('User is trying to do forget password')
        user = us.get_user_by_email(email)
        if user is not None:
            user.password = data.get('password')
            us.update_user(user)
            return jsonify({'status': 'success'}), 200
    return jsonify({'status': 'failed'})

@app.route('/auth/regist', methods=['POST'])
def regist():
    us = UserService()
    email = request.get_json().get('email')
    if us.get_user_by_email(email) is not None:
        print('email already exists: ' + email)
        return jsonify({'status': 'failed', 'message': 'Email already exists'}), 400
    return jsonify({'status': 'success'}), 200

@app.route('/auth/login', methods=['POST'])
def login():
    us = UserService()
    email = request.get_json().get('email')
    password = request.get_json().get('password')

    print(email)
    print(password)

    user = us.user_login(email, password)
    if user is not None:
        print(user.username + ' successfully logged in')
        return jsonify({'status': 'success'})
    print('login failed')
    return jsonify({'status': 'failed'})

@app.route('/test-server', methods=['GET'])
def test_server():
    return jsonify({'status': 'success'}), 200

if __name__ == '__main__':
    SECRET_KEY = secrets.token_bytes(32)
    app.run(host='0.0.0.0', port=5000)
