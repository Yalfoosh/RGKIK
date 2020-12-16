pragma solidity ^0.4.24;

import "./Auction.sol";

contract EnglishAuction is Auction {
    uint256 internal highestBid;
    uint256 internal initialPrice;
    uint256 internal biddingPeriod;
    uint256 internal lastBidTimestamp;
    uint256 internal minimumPriceIncrement;

    address internal highestBidder;

    constructor(
        address _sellerAddress,
        address _judgeAddress,
        Timer _timer,
        uint256 _initialPrice,
        uint256 _biddingPeriod,
        uint256 _minimumPriceIncrement
    ) public Auction(_sellerAddress, _judgeAddress, _timer) {
        initialPrice = _initialPrice;
        biddingPeriod = _biddingPeriod;
        minimumPriceIncrement = _minimumPriceIncrement;
        lastBidTimestamp = time();
    }

    function bid() public payable {
        uint256 timestamp = time();
        uint256 minimumBid = highestBid + minimumPriceIncrement;
        minimumBid = minimumBid > initialPrice ? minimumBid : initialPrice;

        require(
            msg.value >= minimumBid,
            "[ReqFail] Can't bid with so little money."
        );
        require(
            outcome == Outcome.NOT_FINISHED,
            "[ReqFail] Can't bid on an inactive auction."
        );
        require(
            timestamp < lastBidTimestamp + biddingPeriod,
            "[ReqFail] Can't bid after bidding period ended."
        );

        if (highestBidder != address(0)) {
            require(
                address(this).balance >= highestBid,
                "[ReqFail] This shouldn't ever happen - someone scammed the auction!"
            );

            highestBidder.transfer(highestBid);
        }

        require(
            address(this).balance >= msg.value,
            "[ReqFail] Can't bid without the money appearing in the contract's balance."
        );

        lastBidTimestamp = timestamp;
        highestBidder = msg.sender;
        highestBid = msg.value;
    }

    function getHighestBidder() public returns (address) {
        if (time() >= lastBidTimestamp + biddingPeriod) {
            finishAuction(
                highestBidder == address(0)
                    ? Outcome.NOT_SUCCESSFUL
                    : Outcome.SUCCESSFUL,
                highestBidder
            );
        }

        return highestBidderAddress;
    }
}
