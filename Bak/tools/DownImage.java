public class DownImage {
	
	public String image_path;
	
	
	public DownImage(String image_path) {
		this.image_path = image_path;
	}
	
	public void loadImage(final ImageCallBack callBack){
		
		final Handler handler = new Handler(){

			@Override
			public void handleMessage(Message msg) {
				// TODO Auto-generated method stub
				super.handleMessage(msg);
				Drawable drawable = (Drawable) msg.obj;
				callBack.getDrawable(drawable);
			}
			
		};
		
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				try {
					Drawable drawable = Drawable.createFromStream(new URL(image_path).openStream(), "");
					
					Message message = Message.obtain();
					message.obj = drawable;
					handler.sendMessage(message);
				} catch (MalformedURLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}).start();
	}


	public interface ImageCallBack{
		public void getDrawable(Drawable drawable);
	}

}
