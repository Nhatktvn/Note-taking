from flask import Flask, request, jsonify
import mysql.connector
from service.UserService import UserService

app = Flask(__name__)



@app.route('/auth/login', methods=['POST'])
def login():
    us = UserService()
    phone = request.get_json().get('username')
    password = request.get_json().get('password')

    print(phone)
    print(password)

    if us.user_login(phone, password) is not None:
        return jsonify({'status': 'success'})
    return jsonify({'status': 'failed'})

if __name__ == '__main__':
    app.run(host='192.168.1.85', port=5000)