package de.codekenner.roadtrip.Cards;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.fima.cardsui.objects.RecyclableCard;

import de.codekenner.roadtrip.R;

/**
 * Created by Marvin on 20.05.2014.
 */
public class ImageCard extends RecyclableCard {

    public ImageCard(String title, String desc, int image) {
        super(title, desc, image);
    }

    @Override
    protected void applyTo(View convertView) {
        ((TextView) convertView.findViewById(R.id.title)).setText(title);
        ((TextView) convertView.findViewById(R.id.description)).setText(desc);
        ((ImageView) convertView.findViewById(R.id.imageView1)).setImageResource(image);
    }

    @Override
    protected int getCardLayoutId() {
        return R.layout.card_image;
    }
}
