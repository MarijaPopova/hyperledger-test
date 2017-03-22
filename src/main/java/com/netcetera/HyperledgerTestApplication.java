package com.netcetera;

import me.grapebaba.hyperledger.fabric.ErrorResolver;
import me.grapebaba.hyperledger.fabric.Fabric;
import me.grapebaba.hyperledger.fabric.Hyperledger;
import me.grapebaba.hyperledger.fabric.models.*;
import me.grapebaba.hyperledger.fabric.models.Error;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import rx.functions.Action1;

import java.util.Collections;

@SpringBootApplication
public class HyperledgerTestApplication implements CommandLineRunner {

	public static void main(String[] args) {
		SpringApplication.run(HyperledgerTestApplication.class, args);
	}

	public void run(String... args) {
		doIt();
	}

	public void doIt() {
		Fabric FABRIC = Hyperledger.fabric("http://10.1.11.11:7050/");
		String ccId = "16e655c0fce6a9882896d3d6d11f7dcd4f45027fd4764004440ff1e61340910a9d67685c4bb723272a497f3cf428e6cf6b009618612220e1471e03b6c0aa76cb";

		FABRIC.createRegistrar(
				Secret.builder()
						.enrollId("alice")
						.enrollSecret("CMS10pEQlB16")
						.build())
				.subscribe(new Action1<OK>() {
					@Override
					public void call(OK ok) {
						System.out.printf("Create registrar ok message:%s\n", ok);
					}
				}, new Action1<Throwable>() {
					@Override
					public void call(Throwable throwable) {
						Error error = ErrorResolver.resolve(throwable, Error.class);
						System.out.printf("Error message:%s\n", error);
					}
				});

		FABRIC.getRegistrar("alice")
				.subscribe(new Action1<OK>() {
					@Override
					public void call(OK ok) {
						System.out.printf("Get registrar ok message:%s\n", ok);
					}
				});

		FABRIC.getRegistrarECERT("alice")
				.subscribe(new Action1<OK>() {
					@Override
					public void call(OK ok) {
						System.out.printf("Get registrar ecert ok message:%s\n", ok);
					}
				});

		FABRIC.getRegistrarTCERT("alice")
				.subscribe(new Action1<OK1>() {
					@Override
					public void call(OK1 ok) {
						for (String okString : ok.getOk()) {
							System.out.printf("Get registrar tcert ok message:%s\n", okString);
						}
					}
				});

		FABRIC.getNetworkPeers().subscribe(new Action1<PeersMessage>() {
			@Override
			public void call(PeersMessage peersMessage) {
				for (PeerEndpoint peerEndpoint : peersMessage.getPeers()) {
					System.out.printf("Peer message:%s\n", peerEndpoint);
				}

			}
		});

		FABRIC.getBlock(0)
				.subscribe(new Action1<Block>() {
					@Override
					public void call(Block block) {
						System.out.printf("Get Block info:%s\n", block);
					}
				}, new Action1<Throwable>() {
					@Override
					public void call(Throwable throwable) {
						Error error = ErrorResolver.resolve(throwable, Error.class);
						System.out.println("Error " + error.getError());
					}
				});

		ChaincodeOpPayload cop =
				ChaincodeOpPayload.builder()
						.jsonrpc("2.0")
						.id(1)
						.method("query")
						.params(
								ChaincodeSpec.builder()
										.chaincodeID(
												ChaincodeID.builder()
														.name(ccId)
														.build())
										.ctorMsg(
												ChaincodeInput.builder()
														.function("read")
														.args(Collections.singletonList("_marbleindex"))
														.build())
										.secureContext("alice")
										.type(ChaincodeSpec.Type.GOLANG)
										.build())
						.build();
		FABRIC.chaincode(cop)
				.subscribe(new Action1<ChaincodeOpResult>() {
					@Override
					public void call(ChaincodeOpResult chaincodeOpResult) {
						System.out.printf("Query chaincode result:%s\n", chaincodeOpResult);
					}
				});


	}
}
