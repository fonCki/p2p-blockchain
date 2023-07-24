module com.example.p2pblockchain {
    requires javafx.controls;
    requires javafx.fxml;
            
                            
    opens com.example.p2pblockchain to javafx.fxml;
    exports com.example.p2pblockchain;
}