import re
import urllib.request
from bs4 import BeautifulSoup
from google.cloud import storage

# Enable Storage
client = storage.Client(project='fashionistagram-66015')

# Reference an existing bucket.
bucket = client.get_bucket('fashionistagram-66015.appspot.com')

url = "http://madam4060.com/"
soup = BeautifulSoup(urllib.request.urlopen("http://madam4060.com/shop/shopbrand.html?xcode=141&type=X"), "html.parser")

i = 10
for link in soup.findAll('img', attrs={'class':'thumb_img'}):
    img_src = link.attrs['src']
    img_url = url + img_src
    img_type = img_src[33:36]
    img_name = img_src[19:31] # 이미지 src에서 / 없애기
    savename = "./test1/" + img_name
    if(img_type != 'gif'):
        urllib.request.urlretrieve(img_url, savename + '.png')
        print(i)
        zebraBlob = bucket.blob('styles/' + '00' + str(i) + '.png')
        zebraBlob.upload_from_filename(filename = savename + '.png')
        i = int(i) + 1
