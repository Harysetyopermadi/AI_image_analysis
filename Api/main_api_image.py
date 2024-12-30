from flask import Flask, request, jsonify
import ollama
import base64
import os

app = Flask(__name__)

@app.route('/question_image', methods=['POST'])
def question_image():
    try:
        # Memeriksa apakah file gambar dalam bentuk Base64 dikirimkan dalam permintaan
        pertanyaan = request.json.get('question')
        gambar = request.json.get('image') 
        if not (gambar and pertanyaan):
            return jsonify({'result': 'No image data found in the request'}), 400

        # Menggunakan model Ollama untuk menganalisis gambar
        response = ollama.chat(
            model='llama3.2-vision',
            messages=[{
                'role': 'user',
                'content': pertanyaan,
                'images': [gambar]
            }]
        )
        
        # Log the response for debugging
        print(response)

        #Extract the content from the response
        message_content = response.get('message', {}).get('content', 'No description available')

        #Return the description in the response
        return jsonify({'result': message_content})
        # print(gambar)
        # return jsonify({'result': pertanyaan})

    except Exception as e:
        return jsonify({'result': str(e)}), 500

@app.route('/analyze_image', methods=['POST'])
def analyze_image():
    try:
        # Memeriksa apakah file gambar dalam bentuk Base64 dikirimkan dalam permintaan
        data = request.json.get('image') 
        if not data:
            return jsonify({'error': 'No image data found in the request'}), 400

        # Menggunakan model Ollama untuk menganalisis gambar
        response = ollama.chat(
            model='llama3.2-vision',
            messages=[{
                'role': 'user',
                'content': 'Gambar apakah ini?',
                'images': [data]
            }]
        )
        
        # Log the response for debugging
        print(response)

        # Extract the content from the response
        message_content = response.get('message', {}).get('content', 'No description available')

        # Return the description in the response
        return jsonify({'result': message_content})

    except Exception as e:
        return jsonify({'error': str(e)}), 500

if __name__ == '__main__':
    app.run(debug=True ,host='192.168.0.3',port=8080)
