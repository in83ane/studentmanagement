from pathlib import Path
import sys,json,math,wave,struct,subprocess
import imageio_ffmpeg
from PIL import Image,ImageDraw,ImageFont
r=Path(__file__).resolve().parents[1];raw=r/'app/src/main/res/raw';raw.mkdir(parents=True,exist_ok=True)
ff=imageio_ffmpeg.get_ffmpeg_exe()
with wave.open('/tmp/lab89-music.wav','w') as w:
 w.setparams((1,2,44100,0,'NONE','not compressed'))
 notes=[261.63,329.63,392,523.25,440,392,329.63,293.66]
 samples=[]
 for i in range(44100*12):
  t=i/44100; f=notes[int(t/.375)%len(notes)]; envelope=min(1,(t%.375)*30)*math.exp(-5*(t%.375))
  samples.append(struct.pack('<h',int(5000*envelope*(math.sin(2*math.pi*f*t)+.25*math.sin(4*math.pi*f*t)))))
 w.writeframes(b''.join(samples))
subprocess.run([ff,'-y','-i','/tmp/lab89-music.wav','-codec:a','libmp3lame','-b:a','128k',str(raw/'music.mp3')],check=True,capture_output=True)
proc=subprocess.Popen([ff,'-y','-f','rawvideo','-vcodec','rawvideo','-pix_fmt','rgb24','-s','640x360','-r','24','-i','-','-an','-vcodec','libx264','-pix_fmt','yuv420p','-movflags','+faststart',str(raw/'campus.mp4')],stdin=subprocess.PIPE,stderr=subprocess.DEVNULL)
font=ImageFont.truetype('/System/Library/Fonts/Helvetica.ttc',26)
small=ImageFont.truetype('/System/Library/Fonts/Helvetica.ttc',17)
for i in range(192):
 im=Image.new('RGB',(640,360),'#dceef8');d=ImageDraw.Draw(im)
 d.ellipse((490,30,550,90),fill='#f5bd4f');d.rectangle((0,270,640,360),fill='#8ab99b')
 for x,h,c in [(50,120,'#638bb0'),(230,155,'#3c668b'),(420,110,'#7196b5')]:
  d.rectangle((x,270-h,x+145,270),fill=c)
  for wx in range(x+15,x+135,30):
   for wy in range(285-h,250,30):d.rectangle((wx,wy,wx+15,wy+16),fill='#e6e9d8')
 d.text((22,18),'STUDENT LIFE',fill='#173858',font=font)
 d.text((22,53),'An original animated campus demo',fill='#173858',font=small)
 d.rectangle((0,310,640,342),fill='#e2d6c1')
 x=int(i*3.3)%700-30;d.ellipse((x,278,x+14,292),fill='#173858');d.line((x+7,292,x+7,312),fill='#173858',width=5)
 stride=int(9*math.sin(i*.35));d.line((x+7,310,x+7+stride,326),fill='#173858',width=4);d.line((x+7,310,x+7-stride,326),fill='#173858',width=4)
 d.text((470,339),f'{i/24:.1f}s / 8.0s',fill='#173858',font=small)
 proc.stdin.write(im.tobytes())
proc.stdin.close();assert proc.wait()==0
# Original vector animations, no third-party assets.
def prop(v):return {'a':0,'k':v}
def anim(a,b,c):
 return {'a':1,'k':[{'t':0,'s':[a],'e':[b],'o':{'x':[.33],'y':[0]},'i':{'x':[.67],'y':[1]}},{'t':30,'s':[b],'e':[c],'o':{'x':[.33],'y':[0]},'i':{'x':[.67],'y':[1]}},{'t':60,'s':[c]}]}
def layer(index,name,shapes,pos,rotation=None):
 return {'ddd':0,'ind':index,'ty':4,'nm':name,'sr':1,'ks':{'o':prop(100),'r':rotation or prop(0),'p':prop(pos),'a':prop([0,0,0]),'s':prop([100,100,100])},'ao':0,'shapes':shapes,'ip':0,'op':60,'st':0,'bm':0}
def shape(kind,size,pos,color):
 return [{'ty':kind,'p':prop(pos),'s':prop(size),'r':prop(8),'nm':kind},{'ty':'fl','c':prop(color),'o':prop(100),'r':1,'bm':0,'nm':'Fill'}]
def save(name,layers):
 (raw/name).write_text(json.dumps({'v':'5.7.4','fr':30,'ip':0,'op':60,'w':200,'h':200,'nm':name,'ddd':0,'assets':[],'layers':layers}))
blue=[.15,.38,.65,1]
ls=[]
for n in range(8):
 angle=n*math.pi/4
 ls.append(layer(n+1,'Orbit dot',shape('el',[16,16],[48*math.cos(angle),48*math.sin(angle)],[.15,.38,.65,.4+n*.07]),[100,100,0],anim(0,180,360)))
save('loading.json',ls)
ls=[layer(1,'Head',shape('el',[29,29],[0,0],blue),[100,39,0]),layer(2,'Body',shape('rc',[21,62],[0,28],blue),[100,60,0])]
for index,kind,x,y,ra,rb in [(3,'Left arm',87,67,-30,30),(4,'Right arm',113,67,30,-30),(5,'Left leg',94,116,25,-25),(6,'Right leg',106,116,-25,25)]:
 ls.append(layer(index,kind,shape('rc',[11,55],[0,24],blue),[x,y,0],anim(ra,rb,ra)))
save('walking.json',ls)
(r/'MEDIA_LICENSE.txt').write_text('music.mp3, campus.mp4, loading.json and walking.json were generated specifically for this coursework. Original synthesized music, vector campus animation and Lottie shapes. No third-party media used. Reuse permitted for this project.\n')
print('Created MP3, MP4 and two Lottie JSON animations')
