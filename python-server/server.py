from flask import Flask, render_template, request, redirect, url_for, flash, abort, jsonify, send_file
from functools import wraps
from base64 import b64decode
import re
import jwt
import os

app = Flask(__name__)
app.config['MAX_CONTENT_LENGTH'] = 200 * 1024 * 1024

secret = 'b37e50cedcd3e3f1ff64f4afc0422084ae694253cf399326868e07a35f4a45fb'
room_list = [] # {"userid": "test", "title": "test", "server_ip": "test", "server_port": 1234, "mic_opt": True, "limit": 4, "password": "test", "inuser": ["test"], "cnt": 1}
user_list = [] # {"userid": "test", "username": "test", "password": "test", "introduction": "test", "img": "test"}
ipv4_pattern = re.compile(
    r'^(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$'
)

def authenticate(func):
    @wraps(func)
    def wrapper(*args, **kwargs):
        auth_token = request.headers.get('Authorization')
        if not auth_token:
            return jsonify({'msg': 'Authentication token is required.'}), 401
        auth_token = auth_token.split('Basic: ')[-1]
        try:
            payload = jwt.decode(auth_token, secret, algorithms=["HS256"])
        except jwt.ExpiredSignatureError:
            return jsonify({'msg': 'Token has expired.'}), 401
        except jwt.InvalidTokenError:
            return jsonify({'msg': 'Invalid token.'}), 401

        return func(*args, **kwargs)

    return wrapper

@app.after_request
def set_response_headers(response):
    response.headers['Cache-Control'] = 'no-cache, no-store, must-revalidate'
    response.headers['Pragma'] = 'no-cache'
    response.headers['Expires'] = '0'
    return response

@app.route("/heartbeat", methods=["GET"])
def heartbeat():
    return jsonify({'msg':'success'}), 200

@app.route("/login", methods=["POST"])
def login():
    required_fields = {
        "userid": lambda x: True if type(x) == str else False,
        "password": lambda x: True if type(x) == str else False,
    }

    data = request.json

    for i in required_fields:
        if not data.get(i):
            return jsonify({'msg':f'{i} is required data'}), 400
        if not required_fields[i](data.get(i)):
            return jsonify({'msg':f'{i} does not fit the format'}), 400

    for i in user_list:
        print(i['userid'],  data['userid'])
        print(i['password'],  data['password'])
        if i['userid'] == data['userid'] and i['password'] == data['password']:
            encoded_data = jwt.encode(payload={"userid": data['userid']}, key=secret, algorithm="HS256")
            return jsonify({'msg':encoded_data}), 200

    return jsonify({'msg':'login fail'}), 400


@app.route("/register", methods=["POST"])
def register():
    required_fields = {
        "userid": lambda x: True if type(x) == str else False,
        "password": lambda x: True if type(x) == str else False,
        "check_password": lambda x: True if type(x) == str else False,
    }

    data = request.json

    if data['check_password'] == "":
        for i in user_list:
            if i['userid'] == data['userid']:
                print({'msg':'already exists'})
                return jsonify({'msg':'already exists'}), 400

        ind = {}
        ind['userid'] = data['userid']
        ind['username'] = data['userid']
        ind['password'] = data['password']
        ind['introduction'] = ''
        ind['img'] = ''
        user_list.append(ind)

        return jsonify({'msg':'ok'}), 200
    else:
        for i in user_list:
            print(i['password'])
            if i['userid'] == data['userid'] and i['password'] == data['check_password']:
                i['password'] = data['password']
                return jsonify({'msg':'ok'}), 200

        return jsonify({'msg': 'fail'}), 400
    return jsonify({'msg': 'fail'}), 400


@app.route("/update", methods=["POST"])
@authenticate
def update():
    required_fields = {
        "server_ip": lambda x: True if type(x) == str and bool(ipv4_pattern.match(x)) else False,
        "server_port": lambda x: True if type(x) == int else False,
        "userid": lambda x: True if type(x) == str else False,
    }

    data = request.json
    userid = jwt.decode(request.headers.get('Authorization').split('Basic: ')[-1], secret, algorithms=["HS256"])['userid']

    for i in required_fields:
        if not data.get(i):
            return jsonify({'msg':f'{i} is required data'}), 400
        if not required_fields[i](data.get(i)):
            return jsonify({'msg':f'{i} does not fit the format'}), 400

    for i in room_list:
        if i['userid'] == userid:
            i['userid'] = data['userid']
            i['server_ip'] = data['server_ip']
            i['server_port'] = data['server_port']
            return jsonify({'msg':'ok'}), 200

    return jsonify({'msg':'fail'}), 400

@app.route("/delete", methods=["POST"])
@authenticate
def delete():
    userid = jwt.decode(request.headers.get('Authorization').split('Basic: ')[-1], secret, algorithms=["HS256"])['userid']
    for i in room_list:
        if i['userid'] == userid:
            room_list.remove(i)
            return jsonify({'msg':'ok'}), 200
        if userid in i['inuser']:
            i['inuser'].remove(userid)
            i['cnt'] = len(i['inuser'])
            return jsonify({'msg':'ok'}), 200

    return jsonify({'msg':'fail'}), 400

@app.route("/create", methods=["POST"])
@authenticate
def create():
    required_fields = {
        "title": lambda x: True if type(x) == str else False,
        "server_ip": lambda x: True if type(x) == str and bool(ipv4_pattern.match(x)) else False,
        "server_port": lambda x: True if type(x) == int else False,
        "mic_opt": lambda x: True if type(x) == bool else False,
        "limit": lambda x: True if type(x) == int else False,
        "password":lambda x: True if type(x) == str else False,
    }

    data = request.json
    userid = jwt.decode(request.headers.get('Authorization').split('Basic: ')[-1], secret, algorithms=["HS256"])['userid']


    print(data)
    for i in required_fields:
        if data.get(i) is None:
            print({'msg':f'{i}f {i} is required data'})
            return jsonify({'msg':f'{i} is required data'}), 400
        if not required_fields[i](data.get(i)):
            print(f'{i} does not fit the format')
            return jsonify({'msg':f'{i} does not fit the format'}), 400

    for i in room_list:
        if i['userid'] == userid:
            return jsonify({'msg':'room already exists'}), 400

    data['userid'] = userid
    inuser = [userid]
    data['inuser'] = inuser
    data['cnt'] = len(inuser)
    room_list.append(data)
    return jsonify({'msg':'ok'}), 200

@app.route("/join", methods=["POST"])
@authenticate
def join():
    ipv4_pattern = re.compile(
        r'^(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$'
    )
    required_fields = {
        "client_ip": lambda x: True if type(x) == str and bool(ipv4_pattern.match(x)) else False,
        "client_port": lambda x: True if type(x) == int else False,
        "server_userid": lambda x: True if type(x) == str else False,
        "password": lambda x: True if type(x) == str else False,
    }

    data = request.json
    userid = jwt.decode(request.headers.get('Authorization').split('Basic: ')[-1], secret, algorithms=["HS256"])['userid']

    for i in required_fields:
        if data.get(i) is None:
            print({'msg':f'{i} is required data'})
            return jsonify({'msg':f'{i} is required data'}), 400
        if not required_fields[i](data.get(i)):
            print(f'{i} does not fit the format')
            return jsonify({'msg':f'{i} does not fit the format'}), 400
    print(data)
    for i in room_list:
        if i['userid'] == data['server_userid']:
            if data['password'] != i['password']:
                return jsonify({'msg':'incorrect password'}), 400
            if i['cnt'] < i['limit']:
                i['inuser'].append(userid)
                i['cnt'] = len(i['inuser'])
                return jsonify({'data':i}), 200
            else:
                return jsonify({'msg':'The room is full.'}), 400


    return jsonify({'msg':"can't find a room"}), 400

# GET == app start, POST = update
@app.route("/profile", methods=["POST","GET"])
@authenticate
def profile():
    print(user_list)
    userid = jwt.decode(request.headers.get('Authorization').split('Basic: ')[-1], secret, algorithms=["HS256"])['userid']
    if request.method == 'POST':
        data = request.json
        for i in user_list:
            if i['userid'] == userid:
                i['username'] = data['username']
                if data['password'] != "":
                    i['password'] = data['password']
                i['introduction'] = data['introduction']
                if data['img'] != "":
                    with open(f'user_image/{userid}.png', 'wb') as f:
                        f.write(b64decode(data['img']))
                    i['img'] = f'{server_domain}image/{userid}'
                else:
                    i['img'] = ""
                print(i)
                return jsonify({'msg':'ok'}), 200

        return jsonify({'msg':'join'}), 200

    if request.method == "GET":
        for i in user_list:
            if i['userid'] == userid:
                return jsonify({'data':i}), 200
        data = {}
        data['userid'] = userid
        data['username'] = userid
        data['password'] = ''
        data['introduction'] = ''
        data['img'] = ''
        user_list.append(data)

        return jsonify({'msg':'fuser'}), 200

@app.route("/image/<filename>", methods=["GET"])
def get_image(filename):
        return send_file(f'user_image/{filename}.png')

@app.route("/nat", methods=["POST"])
@authenticate
def nat():
    required_fields = {
        "ip": lambda x: True if type(x) == str and bool(ipv4_pattern.match(x)) else False,
        "port": lambda x: True if type(x) == int else False
        }
    data = request.json
    userid = jwt.decode(request.headers.get('Authorization').split('Basic: ')[-1], secret, algorithms=["HS256"])['userid']

    if data.get('error'):
        return jsonify({'msg':'socket error'}), 400
    for i in required_fields:
        if not data.get(i):
            return jsonify({'msg':f'{i} is required data'}), 400
        if not required_fields[i](data.get(i)):
            return jsonify({'msg':f'{i} does not fit the format'}), 400

    for i in room_list:
        if i['userid'] == userid:
            i['server_ip'] = data['ip']
            i['server_port'] = data['port']
            return jsonify({'msg':'ok'}), 200

    return jsonify({'msg':'fail'}), 400

@app.route("/room/<server_userid>", methods=["GET"])
@authenticate
def room_index(server_userid):
    userid = jwt.decode(request.headers.get('Authorization').split('Basic: ')[-1], secret, algorithms=["HS256"])['userid']
    for i in room_list:
        if i['userid'] == server_userid and userid in i['inuser']:
            return jsonify({'inuser':i['inuser'],'mic_opt':i['mic_opt']}), 200

    return jsonify({'msg':'error'}), 400

@app.route("/user/<userid>", methods=["GET"])
@authenticate
def get_user(userid):
    for i in user_list:
        if i['userid'] == userid:
            return jsonify({'data':{'userid':i['userid'],'username':i['username'], 'introduction':i['introduction'], 'img':i['img']}}), 200

    return jsonify({'msg':'error'}), 400

@app.route("/", methods=["GET"])
@authenticate
def _list():
    return {'data':room_list}

@app.route("/user", methods=["GET"])
def user_l():
    return {'data':user_list}

if __name__ == "__main__":
    try:
        os.mkdir('user_image')
    except:
        pass

    server_domain = input(" input server url >> ")
    if not server_domain.startswith('http://') or not server_domain.startswith('https://'):
        server_domain = 'http://' + server_domain
    if not server_domain.endswith('/'):
        server_domain += '/'
    print(server_domain)

    app.run(debug=False, host='0.0.0.0', port=3333)  
