from flask import Flask, request, jsonify, send_from_directory
from gevent import pywsgi
import json
import os
import base64
from detectfb import detectImg
import time
import io
from PIL import Image
from PIL import ImageFile
import MySQLdb


app = Flask(__name__)
currentPath = os.getcwd()


'''
def return_img_stream(img_local_path):
    """
    工具函数:
    获取本地图片流
    :param img_local_path:文件单张图片的本地绝对路径
    :return: 图片流
    """
    img_stream = ''
    with open(img_local_path, 'rb') as img_f:
        img_stream = img_f.read()
        img_stream = base64.b64encode(img_stream)
    return img_stream
'''


class MyEncoder(json.JSONEncoder):
    def default(self, obj):
        if isinstance(obj, bytes):
            return str(obj, encoding='utf-8');
        return json.JSONEncoder.default(self, obj)


'''
@app.route('/getHandbook', methods=['GET'])
def getHandbook():
    return send_from_directory('', 'insect.json')
'''


def compressImg(outfile, mb=600, quality=85, k=0.9):
    """不改变图片尺寸压缩到指定大小
    :param outfile: 压缩文件保存地址
    :param mb: 压缩目标，KB
    :param step: 每次调整的压缩比率
    :param quality: 初始压缩比率
    :return: 压缩文件地址，压缩文件大小
    """

    o_size = os.path.getsize(outfile) // 1024
    if o_size <= mb:
        return outfile

    ImageFile.LOAD_TRUNCATED_IMAGES = True
    while o_size > mb:
        im = Image.open(outfile)
        x, y = im.size
        out = im.resize((int(x * k), int(y * k)), Image.ANTIALIAS)
        try:
            out.save(outfile, quality=quality)
        except Exception as e:
            break
        o_size = os.path.getsize(outfile) // 1024


@app.route('/getDetect', methods=['GET', 'POST'])
def detect():
    # data = request.json        # 获取 JSON 数据
    imgBase64 = request.form.get('img')
    tempImg = base64.b64decode(imgBase64)
    now = str(time.time())
    file = open(currentPath + '/inputImg/IMG_' + now + '.jpg', 'wb')
    file.write(tempImg)
    file.close()
    imgName = 'IMG_' + now + '.jpg'
    resultDic = detectImg(imgName)   #{'img': imgName, 'name': resultnum, 'nameList': insectList}
    outputPath = currentPath + '/outputImg/' + imgName
    compressImg(outputPath, 200)
    if resultDic['name'] != '图中没有目标':
        conn = MySQLdb.connect(host='127.0.0.1'
                               , user='root'
                               , passwd='09183165'
                               , db='forest_insect'
                               , charset='utf8')
        cursor = conn.cursor()
        areaInfo = ''
        insects = resultDic['nameList']
        for item in insects:
            sql = "SELECT area, occurrence_time from insect_info where name=\"{0}\"".format(item)
            cursor.execute(sql)
            data = cursor.fetchone()
            areaInfo += (item + '：' + data[0] + '\n' + data[1] + '\n')
        resultDic.pop('nameList')
        temp = resultDic['name']
        temp += areaInfo
        resultDic['name'] = temp
        cursor.close()
        conn.close()
    return json.dumps(resultDic, cls=MyEncoder, indent=4)


@app.route('/city', methods=['GET', 'POST'])
def getCityInsects():
    requestCity = request.form.get("city")
    conn = MySQLdb.connect(host='127.0.0.1'
                           , user='root'
                           , passwd='09183165'
                           , db='forest_insect'
                           , charset='utf8')
    cursor = conn.cursor()
    sql = "SELECT * from citys where city=\"{0}\"".format(requestCity)
    cursor.execute(sql)
    data = cursor.fetchone()
    cursor.close()
    conn.close()
    if data is None:
        return "该地区暂未收录，敬请期待后续更新"
    return requestCity + "：" + data[1]


@app.route('/handbook', methods=['GET', 'POST'])
def getHandbook():
    insectFamily = request.form.get("family")
    conn = MySQLdb.connect(host='127.0.0.1'
                           , user='root'
                           , passwd='09183165'
                           , db='forest_insect'
                           , charset='utf8')
    cursor = conn.cursor(cursorclass = MySQLdb.cursors.DictCursor)
    if insectFamily == 'all':
        sql = "SELECT * from insect_info"
    else:
        sql = "SELECT * from insect_info where insect_family=\"{0}\"".format(insectFamily)
    cursor.execute(sql)
    data = cursor.fetchall()
    cursor.close()
    conn.close()
    if len(data) == 0 :
        temp = 'null'
        return temp
    else:
        resultJson = []
        for item in data:
            result = {}
            result['name'] = item['name']
            result['img_path'] = item['img_path']
            result['info'] = str(item['insect_order']) + '，' + str(item['insect_family']) + '，' + str(item['insect_genus']) \
                         + '\n' + str(item['feature']) + '\n' + str(item['area']) + '\n' + str(item['harm'])
            resultJson.append(result)
        return json.dumps(resultJson, cls=MyEncoder, indent=4)



@app.route('/result/<filename>/')
def getResult(filename):
    return send_from_directory(currentPath + '/outputImg', filename)


@app.route('/handbookinfo/<filename>/')
def getInfo(filename):
    return send_from_directory(currentPath + '/handbookinfo', filename)


@app.route('/handbookimg/<filename>/')
def getImg(filename):
    return send_from_directory(currentPath + '/handbookimg', filename)


server = pywsgi.WSGIServer(('0.0.0.0', 5000), app)
server.serve_forever()
