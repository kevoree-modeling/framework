package org.kevoree.modeling.infer;

import org.junit.Test;
import org.kevoree.modeling.KCallback;
import org.kevoree.modeling.KModel;
import org.kevoree.modeling.KObject;
import org.kevoree.modeling.KObjectInfer;
import org.kevoree.modeling.infer.impl.RecommendationAlg;
import org.kevoree.modeling.infer.impl.StatInferAlg;
import org.kevoree.modeling.meta.KMetaClass;
import org.kevoree.modeling.meta.KMetaEnum;
import org.kevoree.modeling.meta.KMetaModel;
import org.kevoree.modeling.meta.KPrimitiveTypes;
import org.kevoree.modeling.meta.impl.MetaModel;

/**
 * Created by assaad on 16/07/15.
 */
public class Recommandation {
    private KMetaModel createMetaModel() {

        KMetaModel metaModel = new MetaModel("RecommendationMM");

        KMetaClass metaClassUser = metaModel.addMetaClass("User");
        metaClassUser.addAttribute("name", KPrimitiveTypes.STRING);

        KMetaClass metaClassProduct = metaModel.addMetaClass("Product");
        metaClassProduct.addAttribute("name", KPrimitiveTypes.STRING);

        KMetaClass metaClassRating = metaModel.addMetaClass("Rating");
        metaClassRating.addReference("ownerUser",metaClassUser,"userRatings",false);
        metaClassUser.addReference("userRatings", metaClassRating, "ownerUser", true);

        metaClassRating.addReference("ratedProduct",metaClassUser,"productRatings",false);
        metaClassProduct.addReference("productRatings", metaClassRating, "ratedProduct", true);

        metaClassRating.addAttribute("ratingValue", KPrimitiveTypes.DOUBLE);

        KMetaClass recommendationSystem =metaModel.addInferMetaClass("Recommendation", new RecommendationAlg());
        recommendationSystem.addDependency("user",metaClassUser,null);
        recommendationSystem.addDependency("product",metaClassProduct,null);

        recommendationSystem.addOutput("rating",KPrimitiveTypes.DOUBLE);


        return metaModel;

    }

    @Test
    public void recTest(){
        KMetaModel mm = createMetaModel();
        KModel model = mm.model();
        model.connect(new KCallback() {
            @Override
            public void on(Object o) {
                KObject user= model.createByName("User", 0, 0);

                KObjectInfer recommendation = (KObjectInfer) model.createByName("Recommendation", 0, 0);

                //save all users
                KObject user1 = model.createByName("User", 0, 0);
                long uid = user1.uuid(); //get user id to map it with the db ID


                //save all products
                KObject product1 = model.createByName("Product",0,0);
                long pid= product1.uuid(); //get product Id to map it with the db ID

                //set ratings between user and products and train the rec
                // train all ratings
                // train the recommender here, send user object, producut object rating
                //
                // recommendation.train(user1,product1, rating);


            }
        });
    }



}
