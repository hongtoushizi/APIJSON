package apijson.demo.client.view;

import java.util.ArrayList;
import java.util.List;

import zuo.biao.apijson.JSONResponse;
import zuo.biao.library.base.BaseView;
import zuo.biao.library.manager.HttpManager.OnHttpResponseListener;
import zuo.biao.library.model.Entry;
import zuo.biao.library.ui.AlertDialog;
import zuo.biao.library.ui.AlertDialog.OnDialogButtonClickListener;
import zuo.biao.library.ui.GridAdapter;
import zuo.biao.library.ui.WebViewActivity;
import zuo.biao.library.util.ImageLoaderUtil;
import zuo.biao.library.util.Log;
import zuo.biao.library.util.ScreenUtil;
import zuo.biao.library.util.StringUtil;
import zuo.biao.library.util.TimeUtil;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;
import apijson.demo.client.R;
import apijson.demo.client.activity_fragment.MomentActivity;
import apijson.demo.client.activity_fragment.UserActivity;
import apijson.demo.client.application.APIJSONApplication;
import apijson.demo.client.model.Comment;
import apijson.demo.client.model.CommentItem;
import apijson.demo.client.model.Moment;
import apijson.demo.client.model.MomentItem;
import apijson.demo.client.model.User;
import apijson.demo.client.util.HttpRequest;

/**作品View
 * @author Lemon
 * @use
MomentView momentView = new MomentView(context, inflater);
adapter中使用convertView = momentView.getView();//[具体见.ModelAdapter] 或  其它类中使用
containerView.addView(momentView.getConvertView());
momentView.bindView(object);
momentView.setOnClickPictureListener(onClickPictureListener);
momentView.setOnDataChangedListener(onDataChangedListener);object = momentView.getData();//非必需
momentView.setOnClickListener(onClickListener);//非必需
...
 */
public class MomentView extends BaseView<MomentItem> implements OnClickListener
, OnHttpResponseListener, OnDialogButtonClickListener, OnItemClickListener {
	private static final String TAG = "MomentView";

	public interface OnClickPictureListener {
		void onClickPicture(int momentPosition, MomentView momentView, int pictureIndex);
	}

	private OnClickPictureListener onClickPictureListener;
	public void setOnClickPictureListener(OnClickPictureListener onClickPictureListener) {
		this.onClickPictureListener = onClickPictureListener;
	}

	public MomentView(Activity context, Resources resources) {
		super(context, resources);
	}


	//UI显示区(操作UI，但不存在数据获取或处理代码，也不存在事件监听代码)<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<

	private LayoutInflater inflater;


	public View llMomentViewContainer;

	public ImageView ivMomentViewHead;

	public TextView tvMomentViewName;
	public TextView tvMomentViewStatus;

	public TextView tvMomentViewContent;

	public GridView gvMomentView;

	public TextView tvMomentViewDate;

	public View llMomentViewPraise;
	public TextView tvMomentViewPraise;

	public View llMomentViewComment;
	public TextView tvMomentViewComment;

	public ViewGroup llMomentViewCommentContainer;

	@SuppressLint("InflateParams")
	@Override
	public View createView(LayoutInflater inflater) {
		this.inflater = inflater;
		convertView = inflater.inflate(R.layout.moment_view, null);

		llMomentViewContainer = findViewById(R.id.llMomentViewContainer);

		ivMomentViewHead = findViewById(R.id.ivMomentViewHead, this);

		tvMomentViewName = findViewById(R.id.tvMomentViewName, this);
		tvMomentViewStatus = findViewById(R.id.tvMomentViewStatus, this);

		tvMomentViewContent = findViewById(R.id.tvMomentViewContent, this);

		gvMomentView = findViewById(R.id.gvMomentView);

		tvMomentViewDate = findViewById(R.id.tvMomentViewDate);

		llMomentViewPraise = findViewById(R.id.llMomentViewPraise, this);
		tvMomentViewPraise = findViewById(R.id.tvMomentViewPraise);

		llMomentViewComment = findViewById(R.id.llMomentViewComment, this);
		tvMomentViewComment = findViewById(R.id.tvMomentViewComment);

		llMomentViewCommentContainer = findViewById(R.id.llMomentViewCommentContainer);

		return convertView;
	}


	private User user;
	private Moment moment;
	private long momentId;
	private long userId;

	private boolean isCurrentUser;
	private int status;
	@Override
	public void bindView(MomentItem data_){
		llMomentViewContainer.setVisibility(data_ == null ? View.GONE : View.VISIBLE);
		if (data_ == null) {
			Log.w(TAG, "bindView data_ == null >> data_ = new MomentItem();");
			data_ = new MomentItem();
		}
		this.data = data_;
		this.user = data.getUser();
		this.moment = data.getMoment();
		this.momentId = moment.getId();
		this.userId = moment.getUserId();
		this.isCurrentUser = APIJSONApplication.getInstance().isCurrentUser(moment.getUserId());
		this.status = data.getStatus();

		ImageLoaderUtil.loadImage(ivMomentViewHead, user.getHead());

		tvMomentViewName.setText(StringUtil.getTrimedString(user.getName()));
		tvMomentViewStatus.setText(StringUtil.getTrimedString(data.getStatusString()));

		tvMomentViewContent.setVisibility(StringUtil.isNotEmpty(moment.getContent(), true) ? View.VISIBLE : View.GONE);
		tvMomentViewContent.setText(StringUtil.getTrimedString(moment.getContent()));

		tvMomentViewDate.setText(TimeUtil.getSmartDate(moment.getDate()));

		// 图片
		setPicture(moment.getPictureList());
		// 点赞
		setPraise(data.getIsPraised(), data.getPraiseCount());
		// 评论
		setComment(data.getIsCommented(), data.getCommentCount(), data.getCommentItemList());

	}


	/**设置赞
	 * @param joined
	 * @param count
	 */
	private void setPraise(boolean joined, int count) {
		tvMomentViewPraise.setText(count <= 0 ? "点赞" : "" + count);
		tvMomentViewPraise.setTextColor(getColor(joined ? R.color.blue : R.color.black));
	}

	private boolean showComment = true;
	public void setShowComment(boolean showComment) {
		this.showComment = showComment;
	}
	public boolean getShowComment() {
		return showComment;
	}


	/**设置评论
	 * @param joined
	 * @param commentCount 
	 * @param list
	 */
	public void setComment(boolean joined, int total, List<CommentItem> list) {
		int count = list == null ? 0 : list.size();
		if (total < count) {
			Log.e(TAG, "setComment  total < count ! >> total = count;");
			total = count;
		}
		tvMomentViewComment.setText(total <= 0 ? "评论" : "" + total);
		tvMomentViewComment.setTextColor(getColor(joined ? R.color.blue : R.color.black));

		if (showComment == false) {
			Log.i(TAG, "setComment  showComment == false >> return;");
			return;
		}

		llMomentViewCommentContainer.setVisibility(total <= 0 ? View.GONE : View.VISIBLE);
		llMomentViewCommentContainer.removeAllViews();

		if (count > 0) {
			for (CommentItem comment : list) {
				addCommentView(comment);
			}
		}
		if (total > count) {
			addCommentView(new CommentItem(new Comment("查看所有")), true);
		}
	}

	private GridAdapter adapter;
	/**设置图片
	 * @param pictureList 
	 */
	private void setPicture(List<String> pictureList) {
		List<Entry<String, String>> keyValueList = new ArrayList<Entry<String, String>>();
		if (pictureList != null) {
			for (String picture : pictureList) {
				keyValueList.add(new Entry<String, String>(picture, null));
			}
		}
		int pictureNum = keyValueList.size();
		gvMomentView.setVisibility(pictureNum <= 0 ? View.GONE : View.VISIBLE);
		if (pictureNum <= 0) {
			Log.i(TAG, "setList pictureNum <= 0 >> lvModel.setAdapter(null); return;");
			adapter = null;
			gvMomentView.setAdapter(null);
			return;
		}

		gvMomentView.setNumColumns(pictureNum <= 1 ? 1 : 3);
		if (adapter == null) {
			adapter = new GridAdapter(context).setHasName(false);
			gvMomentView.setAdapter(adapter);
		}
		adapter.refresh(keyValueList);
		gvMomentView.setOnItemClickListener(this);

		final int gridViewHeight = (int) (ScreenUtil.getScreenSize(context)[0]
				- convertView.getPaddingLeft() - convertView.getPaddingRight()
				- getDimension(R.dimen.work_view_head_width));
		try {
			if (pictureNum >= 7) {
				gvMomentView.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, gridViewHeight));
			} else if (pictureNum >= 4) {
				gvMomentView.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, (gridViewHeight*2)/3));
			} else if (pictureNum >= 2) {
				gvMomentView.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, gridViewHeight / 3));
			} else {
				gvMomentView.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
			}
		} catch (Exception e) {
			Log.e(TAG, " setPictureGrid  try int gridViewHeight;...>> catch" + e.getMessage());
		}
	}






	/**
	 * @param comment
	 */
	private void addCommentView(CommentItem comment) {
		addCommentView(comment, false);
	}
	/**
	 * @param comment
	 * @param isMore
	 */
	private void addCommentView(final CommentItem comment, final boolean isMore) {
		if (comment == null) {
			Log.e(TAG, "addCommentView comment == null >> return; ");
			return;
		}
		String content = StringUtil.getTrimedString(comment.getComment().getContent());
		if (StringUtil.isNotEmpty(content, true) == false) {
			Log.e(TAG, "addCommentView StringUtil.isNotEmpty(content, true) == false >> return; ");
			return;
		}

		TextView commentItem = (TextView) inflater.inflate(isMore
				? R.layout.comment_view_comment_more_item : R.layout.moment_view_comment_item, null);
		if (isMore) {
			commentItem.setText(content);
		} else {
			((CommentTextView) commentItem).setView(comment, true);
		}
		commentItem.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				toCommentActivity(comment, ! isMore);
			}
		});

		llMomentViewCommentContainer.addView(commentItem);
	}

	/**跳转到所有评论界面
	 * @param isToComment
	 */
	private void toCommentActivity(boolean isToComment) {
		toCommentActivity(null, isToComment);
	}
	/**跳转到所有评论界面
	 * @param comment
	 * @param isToComment comment有效时为true
	 */
	private void toCommentActivity(CommentItem comment, boolean isToComment) {
		if (isLoggedIn() == false) {
			return;
		}
		long userId = comment == null ? 0 : comment.getUser().getId();
		if (userId <= 0 || APIJSONApplication.getInstance().isCurrentUser(userId)) {
			toActivity(MomentActivity.createIntent(context, momentId, isToComment, comment == null ? 0 : comment.getId()));
			return;
		}

		toActivity(MomentActivity.createIntent(context, momentId, isToComment));
	}

	//UI显示区(操作UI，但不存在数据获取或处理代码，也不存在事件监听代码)>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>










	//Data数据区(存在数据获取或处理代码，但不存在事件监听代码)<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<


	@Override
	public MomentItem getData() {//bindView(null)不会使data == null
		return llMomentViewContainer.getVisibility() == View.VISIBLE ? data : null;
	}

	public List<String> getPictureList() {
		return moment == null ? null : moment.getPictureList();
	}

	/**判断是否已登录，如果未登录则弹出登录界面
	 * @return
	 */
	private boolean isLoggedIn() {
		return APIJSONApplication.getInstance().isLoggedIn();
	}


	/**赞
	 * @param toPraise
	 */
	public void praise(boolean toPraise) {
		if (toPraise == data.getIsPraised()) {
			Log.e(TAG, "praiseWork  toPraise == moment.getIsPraise() >> return;");
			return;
		}
		//		setPraise(toPraise, data.getPraiseCount() + (toPraise ? 1 : -1));
		HttpRequest.praiseMoment(momentId, toPraise, toPraise ? HTTP_PRAISE : HTTP_CANCLE_PRAISE, this);
	}

	//Data数据区(存在数据获取或处理代码，但不存在事件监听代码)>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>








	//Event事件监听区(只要存在事件监听代码就是)<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<


	@Override
	public void onDialogButtonClick(int requestCode, boolean isPositive) {
		if (isPositive) {
			HttpRequest.deleteMoment(moment.getId(), HTTP_DELETE, this);
			bindView(null);
		}
	}



	public static final int HTTP_PRAISE = 1;
	public static final int HTTP_CANCLE_PRAISE = 2;
	public static final int HTTP_DELETE = 3;
	@Override
	public void onHttpResponse(int requestCode, String result, Exception e) {
		if (data == null) {
			Log.e(TAG, "onHttpResponse  data == null  >> return;");
			return;
		}
		JSONResponse response = new JSONResponse(result);
		switch (requestCode) {
		case HTTP_PRAISE:
		case HTTP_CANCLE_PRAISE:
			response = response.getJSONResponse(Moment.class.getSimpleName());
			if (JSONResponse.isSucceed(response)) {
				data.setIsPraised(requestCode == HTTP_PRAISE);
				bindView(data);
			} else {
				showShortToast((requestCode == HTTP_PRAISE ? "点赞" : "取消点赞") + "失败，请检查网络后重试");
			}
			break;
			//		case HTTP_DELETE:
			//			if(resultCode == HttpRequest.RESULT_DELETE_WORK_SUCCEED) {
			//				context.sendBroadcast(new Intent(ActionUtil.REFRESH_WORK)
			//				.putExtra(ActionUtil.TYPE, ActionUtil.TYPE_DELETE_WORK)
			//				.putExtra(ActionUtil.RESULT_WORK, Json.toJSONString(moment)));
			//			} else {
			//				bindView(data);
			//			}
			//			break;
		}
	}


	@Override
	public void onClick(View v) {
		if (status == MomentItem.STATUS_PUBLISHING) {
			showShortToast(R.string.publishing);
			return;
		}
		switch (v.getId()) {
		case R.id.ivMomentViewHead:
		case R.id.tvMomentViewName:
			toActivity(UserActivity.createIntent(context, userId));
			break;
		case R.id.tvMomentViewStatus:
			switch (status) {
			case MomentItem.STATUS_PUBLISHING:
				break;
			case MomentItem.STATUS_DELETING:
				break;
			default:
				new AlertDialog(context, "", "", true, 0, this).show();
				break;
			}
			break;
		case R.id.tvMomentViewContent:
			//			toActivity(TextActivity.createIntent(context, moment == null ? null : moment.getContent()));
			break;
		case R.id.tvMomentViewPraise:
			//			toActivity(PraiseListActivity.createIntent(context, PraiseListActivity.TYPE_WORK, momentId));
			break;
		default:
			if (isLoggedIn() == false) {
				return;
			}
			switch (v.getId()) {
			case R.id.llMomentViewPraise:
				praise(! data.getIsPraised());
				break;
			case R.id.llMomentViewComment:
				toCommentActivity(true);
				break;
			default:
				break;
			}
			break;
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		if (status == MomentItem.STATUS_PUBLISHING) {
			showShortToast(R.string.publishing);
			return;
		}
		if (onClickPictureListener != null) {
			onClickPictureListener.onClickPicture(this.position, this, position);
		} else {
			toActivity(WebViewActivity.createIntent(context, null
					, adapter == null ? null : adapter.getItem(position).getKey()));
		}
	}

	//Event事件监听区(只要存在事件监听代码就是)>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>

}