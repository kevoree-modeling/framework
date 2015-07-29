package org.kevoree.modeling;

import org.kevoree.modeling.meta.KMetaClass;
import org.kevoree.modeling.meta.KMetaModel;
import org.kevoree.modeling.meta.KPrimitiveTypes;
import org.kevoree.modeling.meta.impl.MetaModel;

import java.util.Random;

/**
 * Created by assaad on 29/07/15.
 */
public class RecommendationSystemBench {
    private static KMetaModel createMetaModel() {

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

        return metaModel;
    }

    public static void main(String[] arg){
        KMetaModel mm = createMetaModel();
        KModel model = mm.model();
        model.connect(new KCallback() {
            @Override
            public void on(Object o) {
                int maxUsers=23000; //add 0
                int maxProducts=2700; // add 0
                int ratingPerUser=9; // add 0 

                KObject[] users= new KObject[maxUsers];
                for(int i=0;i<maxUsers;i++){
                    users[i]=model.createByName("User",0,0);
                }

                KObject[] products= new KObject[maxProducts];

                for(int i=0;i<maxProducts;i++){
                    products[i]=model.createByName("Product",0,0);
                }

                System.out.println("Users and products created");

                Random random=new Random();
                int[] ratingPos=new int[ratingPerUser];

                for(int i=0;i<maxUsers;i++){
                    for(int j=0;j<ratingPerUser;j++){
                        boolean b;
                        do {
                            ratingPos[j] = random.nextInt(maxProducts);
                            b=false;
                            for(int k=0;k<j;k++){
                                if(ratingPos[k]==ratingPos[j]){
                                    b=true;
                                }
                            }
                        }
                        while (b);
                    }

                    for(int j=0;j<ratingPos.length;j++){
                        KObject rating = model.createByName("Rating",0,0);

                        rating.mutate(KActionType.SET, model.metaModel().metaClassByName("Rating").reference("ownerUser"), users[i]);
                        rating.mutate(KActionType.SET, model.metaModel().metaClassByName("Rating").reference("ratedProduct"), products[ratingPos[j]]);
                        rating.setByName("ratingValue",random.nextDouble()*5);

                    }

                 //   System.out.println(users[i].getRefValuesByName("userRatings").length);

                }
                System.out.println("Done, created: "+maxUsers+" users, "+maxProducts+" products, "+(ratingPerUser*maxUsers)+" ratings!");

            }
        });
    }
}
