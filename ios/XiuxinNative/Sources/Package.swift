// swift-tools-version:5.7
import PackageDescription

let package = Package(
    name: "XiuxinNative",
    platforms: [.iOS(.v15)],
    products: [
        .library(name: "XiuxinNative", targets: ["XiuxinNative"])
    ],
    dependencies: [],
    targets: [
        .target(
            name: "XiuxinNative",
            path: "Sources",
            exclude: ["Info.plist"]
        )
    ]
)
