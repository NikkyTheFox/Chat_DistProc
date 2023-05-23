import socket
import threading
from PyQt5.QtCore import Qt
from PyQt5.QtWidgets import QApplication, QWidget, QVBoxLayout, QTextEdit, QLineEdit, QPushButton


class ChatWidget(QWidget):
    def __init__(self,app):
        super().__init__()
        # Create a TCP socket
        self.app  = app
        self.username = None;

        # Create a vertical layout
        layout = QVBoxLayout()

        # Create a QTextEdit widget to display messages
        self.text_edit = QTextEdit()
        self.text_edit.setReadOnly(True)
        layout.addWidget(self.text_edit)

        # Create a QLineEdit widget for user input
        self.line_edit = QLineEdit()
        self.line_edit.returnPressed.connect(self.send_messages)
        layout.addWidget(self.line_edit)

        # Create a QPushButton to send messages
        self.send_button = QPushButton("Send")
        self.send_button.clicked.connect(self.send_messages)
        layout.addWidget(self.send_button)

        # Set the layout
        self.setLayout(layout)
        self.sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        self.flag = 0
        # Connect to the server
        self.sock.connect(('127.0.0.1', 5000))
        print("Connection establiesh")
        # Start receive_messages thread
        self.recv_thread = threading.Thread(target=self.receive_messages)
        self.recv_thread.start()

    def send_messages(self):
            message =  self.line_edit.text()
            printing_message = message + "\n"
            if(len(message)>0):
                self.sock.sendall(printing_message.encode())
                if(self.username is None):
                    self.username = message
                else:
                    self.text_edit.append(f"{self.username}: {message}")
                self.text_edit.verticalScrollBar().setValue(self.text_edit.verticalScrollBar().maximum())
                self.line_edit.clear()
            else:
                self.flag = 1
                self.recv_thread.join()
                self.sock.close()
                app.quit()
    def receive_messages(self):
        self.sock.settimeout(1)
        while True:
            if self.flag:
                break
            try:
                server_reply = self.sock.recv(2000)
            except socket.timeout:
                continue
            if not server_reply:
                break
            self.text_edit.append(server_reply.decode().rstrip())
            self.text_edit.verticalScrollBar().setValue(self.text_edit.verticalScrollBar().maximum())

if __name__ == '__main__':

    app = QApplication([])
    widget = ChatWidget(app)

    widget.show()
    app.exec_()